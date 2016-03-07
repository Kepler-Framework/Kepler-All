package com.kepler.connection.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerException;
import com.kepler.KeplerTimeoutException;
import com.kepler.ack.AckTimeOut;
import com.kepler.ack.impl.AckFuture;
import com.kepler.admin.transfer.Collector;
import com.kepler.channel.ChannelContext;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Connect;
import com.kepler.connection.Connects;
import com.kepler.connection.handler.CodecHeader;
import com.kepler.connection.handler.DecoderHandler;
import com.kepler.connection.handler.EncoderHandler;
import com.kepler.host.Host;
import com.kepler.host.HostLocks;
import com.kepler.host.HostsContext;
import com.kepler.host.impl.SegmentLocks;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.serial.Serials;
import com.kepler.service.Quiet;
import com.kepler.token.TokenContext;
import com.kepler.traffic.Traffic;

/**
 * Client 2 Service Connection
 * 
 * @author kim 2015年7月10日
 */
/**
 * @author kim
 *
 * 2016年2月10日
 */
public class DefaultConnect implements Connect {

	/**
	 * 连接超时
	 */
	private static final int TIMEOUT = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".timeout", 5000);

	/**
	 * 黏包最大长度
	 */
	private static final int FRAGEMENT = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".fragement", Integer.MAX_VALUE);

	/**
	 * 发送/接受缓冲区大小
	 */
	private static final int BUFFER_SEND = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".buffer_send", Integer.MAX_VALUE);

	private static final int BUFFER_RECV = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".buffer_recv", Integer.MAX_VALUE);

	/**
	 * 监听待重连线程数量
	 */
	private static final int ESTABLISH_THREAD = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".establish_thread", 1);

	/**
	 * 是否允许本地回路
	 */
	private static final boolean ESTABLISH_LOOP = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".establish_loop", true);

	/**
	 * 是否使用共享Shared EventLoopGroup
	 */
	private static final boolean EVENTLOOP_SHARED = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".eventloop_shared", true);

	/**
	 * EventLoopGroup线程数量
	 */
	private static final int EVENTLOOP_THREAD = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".eventloop_thread", Runtime.getRuntime().availableProcessors() * 2);

	private static final ChannelFactory<SocketChannel> FACTORY = new DefaultChannelFactory<SocketChannel>(NioSocketChannel.class);

	private static final Log LOGGER = LogFactory.getLog(DefaultConnect.class);

	/**
	 * 共享EventLoopGroup, 如果没有开启则为Null
	 */
	private final EventLoopGroup shared = DefaultConnect.EVENTLOOP_SHARED ? new NioEventLoopGroup(DefaultConnect.EVENTLOOP_THREAD) : null;

	private final InitializerFactory inits = new InitializerFactory();

	private final AtomicBoolean shutdown = new AtomicBoolean();

	/**
	 * 建立连接任务,无状态
	 */
	private final Runnable establish = new EstablishRunnable();

	private final HostLocks locks = new SegmentLocks();

	/**
	 * ACK
	 */
	private final Acks acks = new Acks();

	private final Host local;

	private final Quiet quiet;

	private final Traffic traffic;

	private final Serials serials;

	private final Profile profiles;

	private final Connects connects;

	private final AckTimeOut timeout;

	private final TokenContext token;

	private final Collector collector;

	private final HostsContext context;

	private final ChannelContext channels;

	private final ThreadPoolExecutor threads;

	public DefaultConnect(Host local, Quiet quiet, Serials serials, Traffic traffic, Profile profiles, Connects connects, TokenContext token, AckTimeOut timeout, HostsContext context, ChannelContext channels, Collector collector, ThreadPoolExecutor threads) {
		super();
		this.local = local;
		this.token = token;
		this.quiet = quiet;
		this.serials = serials;
		this.threads = threads;
		this.traffic = traffic;
		this.context = context;
		this.timeout = timeout;
		this.connects = connects;
		this.channels = channels;
		this.profiles = profiles;
		this.collector = collector;
	}

	public void init() {
		// 开启重连线程
		for (int index = 0; index < DefaultConnect.ESTABLISH_THREAD; index++) {
			this.threads.execute(this.establish);
		}
		// 黏包
		this.inits.add(new LengthFieldPrepender(CodecHeader.DEFAULT));
		// 编码/解码
		this.inits.add(new EncoderHandler(DefaultConnect.this.traffic, DefaultConnect.this.serials, Request.class));
		this.inits.add(new DecoderHandler(DefaultConnect.this.traffic, DefaultConnect.this.serials, Response.class));
	}

	public void destroy() throws Exception {
		this.shutdown.set(true);
		this.release4shared();
	}

	/**
	 * 关闭共享EventLoopGroup(如果开启)
	 * @throws Exception
	 */
	private void release4shared() throws Exception {
		if (DefaultConnect.EVENTLOOP_SHARED && !this.shared.isShutdown()) {
			this.shared.shutdownGracefully().sync();
			DefaultConnect.LOGGER.info("Shutdown shared eventloop: " + this.shared + " ... ");
		}
	}

	/**
	 * 如果非共享EventLoopGroup则关闭当前EventLoopGroup
	 * 
	 * @param boot
	 * @param host
	 * @throws Exception
	 */
	private void release4private(Bootstrap boot, Host host) throws Exception {
		// 私有EventLoop才释放
		if (!DefaultConnect.EVENTLOOP_SHARED && !boot.group().isShutdown()) {
			boot.group().shutdownGracefully().sync();
			DefaultConnect.LOGGER.warn("Shutdown private eventloop for host: " + host + " ... ");
		}
	}

	private void banAndRelease(ChannelInvoker invoker) throws Exception {
		this.context.ban(invoker.host());
		invoker.releaseAtOnce();
	}

	private void banAndRelease(Host host) throws Exception {
		// 加入Ban名单(Close并不意味着连接永远移除.只要ZK中未注销, 对应Host将再次尝试重连)
		this.context.ban(host);
		// 如果多个请求(Request)同时出现故障并关闭导致再次返回Invoker为Null
		ChannelInvoker invoker = this.channels.del(host);
		if (invoker != null) {
			invoker.release();
		}
	}

	public void connect(Host host) throws Exception {
		synchronized (this.locks.get(host)) {
			// 1个Host仅允许建立1个连接
			if (!this.channels.contain(host)) {
				this.connect(new InvokerHandler(new Bootstrap(), host));
			}
		}
		// 连接成功或已连接则激活该Host所有服务
		this.context.active(host);
	}

	/**
	 * 如果开启共享则使用共享
	 * 
	 * @return
	 */
	private EventLoopGroup eventloop() {
		return DefaultConnect.EVENTLOOP_SHARED ? this.shared : new NioEventLoopGroup(DefaultConnect.EVENTLOOP_THREAD);
	}

	private void connect(InvokerHandler invoker) throws Exception {
		try {
			// 是否为回路IP
			SocketAddress remote = new InetSocketAddress(invoker.host().loop(this.local) && DefaultConnect.ESTABLISH_LOOP ? Host.LOOP : invoker.host().host(), invoker.host().port());
			invoker.bootstrap().group(this.eventloop()).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DefaultConnect.TIMEOUT).channelFactory(DefaultConnect.FACTORY).handler(DefaultConnect.this.inits.factory(invoker)).remoteAddress(remote).connect().sync();
			// 连接成功, 加入通道. 异常则跳过
			this.channels.put(invoker.host(), invoker);
		} catch (Throwable e) {
			DefaultConnect.LOGGER.info("Connect (to " + invoker.host().address() + ") failed ...", e);
			// 关闭并尝试重连
			this.banAndRelease(invoker);
			throw e;
		}
	}

	// 非共享
	private class InvokerHandler extends ChannelInboundHandlerAdapter implements ChannelInvoker {

		private final Host target;

		private final Bootstrap bootstrap;

		private ChannelHandlerContext ctx;

		private InvokerHandler(Bootstrap bootstrap, Host target) {
			super();
			this.target = target;
			this.bootstrap = bootstrap;
		}

		public Bootstrap bootstrap() {
			return this.bootstrap;
		}

		public Host host() {
			return this.target;
		}

		public void close() {
			// 仅在通道尚处激活状态时关闭
			if (this.ctx != null && this.ctx.channel().isActive()) {
				this.ctx.close().addListener(ExceptionListener.TRACE);
			}
		}

		public void release() {
			// 禁止在EventLoop线程关闭Boostrap, 会造成死锁
			DefaultConnect.this.threads.execute(new ReleaseRunnable(this.bootstrap, this.target));
		}

		public void releaseAtOnce() {
			try {
				// 在当前线程中关闭
				DefaultConnect.this.release4private(this.bootstrap, this.target);
			} catch (Exception e) {
				DefaultConnect.LOGGER.error(e.getMessage(), e);
			}
		}

		@Override
		public boolean actived() {
			return true;
		}

		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultConnect.LOGGER.info("Connect active (" + DefaultConnect.this.local + " to " + this.target + ") ...");
			// 初始化赋值
			(this.ctx = ctx).fireChannelActive();
		}

		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			DefaultConnect.LOGGER.info("Connect inactive (" + DefaultConnect.this.local + " to " + this.target + ") ...");
			DefaultConnect.this.banAndRelease(this.target);
			ctx.fireChannelInactive();
		}

		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			this.exceptionPrint(cause);
			// 关闭通道, 并启动Inactive
			ctx.close().addListener(ExceptionListener.TRACE);
		}

		/**
		 * 框架异常使用Error日志
		 * 
		 * @param cause
		 */
		private void exceptionPrint(Throwable cause) {
			if (KeplerException.class.isAssignableFrom(cause.getClass())) {
				DefaultConnect.LOGGER.error(cause.getMessage(), cause);
			} else {
				DefaultConnect.LOGGER.debug(cause.getMessage(), cause);
			}
		}

		public Object invoke(Request request) throws Throwable {
			// DefaultConnect.this.token.set(request, this.target.token())增加Token
			AckFuture future = new AckFuture(DefaultConnect.this.collector, DefaultConnect.this.local, this.target, DefaultConnect.this.token.set(request, this.target.token()), DefaultConnect.this.profiles, DefaultConnect.this.quiet);
			try {
				// 加入ACK -> 发送消息 -> 等待ACK
				this.ctx.writeAndFlush(DefaultConnect.this.acks.put(future).request()).addListener(ExceptionListener.TRACE);
				// 如果为Future或@Async则立即返回, 负责线程等待
				return future.request().async() ? future : future.get();
			} catch (Throwable exception) {
				// 任何异常均释放ACK
				DefaultConnect.this.acks.del(request.ack());
				// Timeout处理
				this.timeout(future, exception);
				throw exception;
			}
		}

		/**
		 * Timeout处理, DefaultConnect.this.collector.peek(ack).timeout()当前周期Timeout次数
		 * 
		 * @param request
		 * @param ack
		 * @param exception
		 */
		private void timeout(AckFuture ack, Throwable exception) {
			// 仅处理KeplerTimeoutException
			if (KeplerTimeoutException.class.isAssignableFrom(exception.getClass())) {
				DefaultConnect.this.timeout.timeout(this, ack, DefaultConnect.this.collector.peek(ack).timeout());
			}
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			Response response = Response.class.cast(message);
			// 移除ACK
			AckFuture future = DefaultConnect.this.acks.del(response.ack());
			// 如获取不到ACK表示已超时
			if (future != null) {
				future.response(response);
			} else {
				DefaultConnect.LOGGER.warn("Missing ack for response: " + response.ack() + " (" + this.target + "), may be timeout ...");
			}
		}
	}

	private class InitializerFactory {

		private final List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();

		public void add(ChannelHandler handler) {
			this.handlers.add(handler);
		}

		public ChannelInitializer<SocketChannel> factory(final InvokerHandler handler) {
			return new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel channel) throws Exception {
					// 指定读写缓存
					channel.config().setReceiveBufferSize(DefaultConnect.BUFFER_RECV);
					channel.config().setSendBufferSize(DefaultConnect.BUFFER_SEND);
					channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
					channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(DefaultConnect.FRAGEMENT, 0, CodecHeader.DEFAULT, 0, CodecHeader.DEFAULT));
					for (ChannelHandler each : InitializerFactory.this.handlers) {
						channel.pipeline().addLast(each);
					}
					channel.pipeline().addLast(handler);
				}
			};
		}
	}

	private class ReleaseRunnable implements Runnable {

		private final Bootstrap bootstrap;

		private final Host host;

		private ReleaseRunnable(Bootstrap bootstrap, Host host) {
			super();
			this.bootstrap = bootstrap;
			this.host = host;
		}

		@Override
		public void run() {
			try {
				DefaultConnect.this.release4private(this.bootstrap, this.host);
			} catch (Exception e) {
				DefaultConnect.LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 重连线程
	 * 
	 * @author kim 2015年7月20日
	 */
	private class EstablishRunnable implements Runnable {
		@Override
		public void run() {
			while (!DefaultConnect.this.shutdown.get()) {
				try {
					Host host = DefaultConnect.this.connects.get();
					if (host != null) {
						DefaultConnect.this.connect(host);
					}
				} catch (Throwable e) {
					DefaultConnect.LOGGER.debug(e.getMessage(), e);
				}
			}
			DefaultConnect.LOGGER.warn(this.getClass() + " shutdown on thread (" + Thread.currentThread().getId() + ")");
		}
	}

	private class Acks {

		private final Map<Integer, AckFuture> waitings = new ConcurrentHashMap<Integer, AckFuture>();

		public AckFuture put(AckFuture future) {
			this.waitings.put(future.request().ack(), future);
			return future;
		}

		public AckFuture del(Integer ack) {
			return this.waitings.remove(ack);
		}
	}
}
