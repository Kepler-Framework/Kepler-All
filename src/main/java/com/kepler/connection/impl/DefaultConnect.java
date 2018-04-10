package com.kepler.connection.impl;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerException;
import com.kepler.KeplerNetworkException;
import com.kepler.ack.AckTimeOut;
import com.kepler.ack.Acks;
import com.kepler.ack.impl.AckFuture;
import com.kepler.admin.transfer.Collector;
import com.kepler.channel.ChannelContext;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Connect;
import com.kepler.connection.Connects;
import com.kepler.connection.codec.CodecHeader;
import com.kepler.connection.codec.Decoder;
import com.kepler.connection.codec.Encoder;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.host.Host;
import com.kepler.host.HostsContext;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.service.Quiet;
import com.kepler.token.TokenContext;
import com.kepler.trace.Trace;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * Client 2 Service Connection
 * 
 * @author kim 2015年7月10日
 */
public class DefaultConnect implements Connect {

	/**
	 * 连接超时
	 */
	private static final int TIMEOUT = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".timeout", 5000);

	/**
	 * 等待预警
	 */
	private static final int WAIT_WARN = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".wait_warn", 50);

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
	 * 是否使用EVENT_LOOP线程解码报文
	 */
	private static final boolean EVENTLOOP_DECODE = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".eventloop_decode", false);

	/**
	 * EventLoopGroup线程数量
	 */
	private static final int EVENTLOOP_THREAD = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".eventloop_thread", Runtime.getRuntime().availableProcessors() * 2);

	/**
	 * 可写测试
	 */
	private static final boolean WRITE_WATER = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".write_water", false);

	private static final int WRITE_WATERWATER_LOW = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".write_water_low", 32768);

	private static final int WRITE_WATERWATER_HIGH = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".write_water_high", 65536);

	private static final boolean IDLE_CLOSE = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".idle_close", true);

	private static final short IDLE_ALL = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".idle_all", Short.MAX_VALUE);

	private static final short IDLE_READ = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".idle_read", Short.MAX_VALUE);

	private static final short IDLE_WRITE = PropertiesUtils.get(DefaultConnect.class.getName().toLowerCase() + ".idle_write", Short.MAX_VALUE);

	private static final AttributeKey<AcksImpl> ACKS = AttributeKey.newInstance("ACKS");

	private static final Log LOGGER = LogFactory.getLog(DefaultConnect.class);

	/**
	 * 共享EventLoopGroup, 如果没有开启则为Null
	 */
	private final EventLoopGroup shared = DefaultConnect.EVENTLOOP_SHARED ? new NioEventLoopGroup(DefaultConnect.EVENTLOOP_THREAD) : null;

	private final InitializerFactory inits = new InitializerFactory();

	/**
	 * 建立连接任务,无状态
	 */
	private final Runnable establish = new EstablishRunnable();

	private final Host local;

	private final Quiet quiet;

	private final Encoder encoder;

	private final Decoder decoder;

	private final Profile profiles;

	private final Connects connects;

	private final AckTimeOut timeout;

	private final TokenContext token;

	private final Collector collector;

	private final HostsContext context;

	private final ChannelContext channels;

	private final FieldsAnalyser analyser;

	private final ThreadPoolExecutor threads;

	volatile private boolean shutdown;

	public DefaultConnect(Host local, Quiet quiet, Encoder encoder, Decoder decoder, Profile profiles, Connects connects, TokenContext token, AckTimeOut timeout, HostsContext context, ChannelContext channels, Collector collector, FieldsAnalyser analyser, ThreadPoolExecutor threads) {
		super();
		this.local = local;
		this.token = token;
		this.quiet = quiet;
		this.encoder = encoder;
		this.decoder = decoder;
		this.threads = threads;
		this.context = context;
		this.timeout = timeout;
		this.analyser = analyser;
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
	}

	public void destroy() throws Exception {
		this.shutdown = true;
		this.release4shared();
	}

	/**
	 * 关闭共享EventLoopGroup(如果开启)
	 * 
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

	/**
	 * 释放通道(异步), 并将主机加入Ban名单后重连
	 * 
	 * @param host
	 * @throws Exception
	 */
	private void release(Host host) throws Exception {
		// 如果多个请求(Request)同时出现故障并关闭导致再次返回Invoker为Null
		ChannelInvoker invoker = this.channels.del(host);
		if (invoker != null) {
			invoker.release();
		}
		// 加入Ban名单(Close并不意味着连接永远移除.只要ZK中未注销, 对应Host将再次尝试重连)
		this.context.ban(host);
	}

	public void connect(Host host) throws Exception {
		// IP锁. 1个Host仅允许建立1个连接
		synchronized (host.host().intern()) {
			if (!this.channels.contain(host)) {
				this.connect(new InvokerHandler(new Bootstrap(), this.local, host, this.channels));
			} else {
				DefaultConnect.LOGGER.warn("Host: " + host + " already connected ...");
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
			SocketAddress remote = new InetSocketAddress(invoker.remote().loop(this.local) && DefaultConnect.ESTABLISH_LOOP ? Host.LOOP : invoker.remote().host(), invoker.remote().port());
			invoker.bootstrap().group(this.eventloop()).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DefaultConnect.TIMEOUT).channelFactory(DefaultChannelFactory.INSTANCE_CLIENT).handler(DefaultConnect.this.inits.factory(invoker)).remoteAddress(remote).connect().sync();
			// 连接成功, 加入通道. 异常则跳过
		} catch (Throwable e) {
			DefaultConnect.LOGGER.info("Connect " + invoker.remote().address() + "[sid=" + invoker.remote().sid() + "] failed ...", e);
			// 关闭并尝试重连
			this.context.ban(invoker.remote());
			invoker.releaseAtOnce();
			throw e;
		}
	}

	// 非共享
	private class InvokerHandler extends ChannelInboundHandlerAdapter implements ChannelInvoker {

		private final Host local;

		private final Host remote;

		private final Bootstrap bootstrap;

		volatile private ChannelHandlerContext ctx;

		private final ChannelContext channels;

		private InvokerHandler(Bootstrap bootstrap, Host local, Host remote, ChannelContext channels) {
			super();
			this.local = local;
			this.remote = remote;
			this.bootstrap = bootstrap;
			this.channels = channels;
		}

		public Bootstrap bootstrap() {
			return this.bootstrap;
		}

		public Host remote() {
			return this.remote;
		}

		public Host local() {
			return this.local;
		}

		public void close() {
			// 仅在通道尚处激活状态时关闭
			if (this.ctx != null && this.ctx.channel().isActive()) {
				this.ctx.close().addListener(ExceptionListener.listener(this.ctx));
			}
		}

		public void release() {
			// 异步关闭
			DefaultConnect.this.threads.execute(new ReleaseRunnable(this.bootstrap, this.remote));
		}

		public void releaseAtOnce() {
			try {
				// 同步关闭
				DefaultConnect.this.release4private(this.bootstrap, this.remote);
			} catch (Exception e) {
				DefaultConnect.LOGGER.error(e.getMessage(), e);
			}
		}

		@Override
		public boolean actived() {
			return true;
		}

		/**
		 * 通道写入水位检查
		 */
		private void water4check() {
			// 通道可读检查, 窗口关闭则抛出异常
			if (DefaultConnect.WRITE_WATER && !this.ctx.channel().isWritable()) {
				throw new KeplerNetworkException("Channel can not writable. [from=" + this.ctx.channel().localAddress() + "][to=" + this.ctx.channel().remoteAddress() + "]");
			}
		}

		/**
		 * 通道写入水位配置
		 */
		private void water4config() {
			// 指定写入流量上下限
			if (DefaultConnect.WRITE_WATER) {
				this.ctx.channel().config().setWriteBufferLowWaterMark(DefaultConnect.WRITE_WATERWATER_LOW);
				this.ctx.channel().config().setWriteBufferHighWaterMark(DefaultConnect.WRITE_WATERWATER_HIGH);
			}
		}

		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultConnect.LOGGER.info("Connect active (" + this.local + " to " + this.remote + ") ...");
			// 初始化赋值
			(this.ctx = ctx).channel().attr(DefaultConnect.ACKS).set(new AcksImpl());
			this.water4config();
			this.channels.put(this.remote(), this);
			this.ctx.fireChannelActive();
		}

		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			DefaultConnect.LOGGER.info("Connect inactive (" + this.local + " to " + this.remote + ") ...");
			DefaultConnect.this.release(this.remote);
			ctx.fireChannelInactive();
		}

		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (DefaultConnect.IDLE_CLOSE && evt instanceof IdleStateEvent) {
				DefaultConnect.LOGGER.warn("Idle (" + IdleStateEvent.class.cast(evt).state() + ") connection closed. [local=" + ctx.channel().localAddress() + "][remote=" + ctx.channel().remoteAddress() + "]");
				// 关闭通道, 并启动Inactive
				ctx.close().addListener(ExceptionListener.listener(this.ctx));
			}
		}

		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// 框架异常使用Error日志
			if (KeplerException.class.isAssignableFrom(cause.getClass())) {
				DefaultConnect.LOGGER.error(cause.getMessage(), cause);
			} else {
				DefaultConnect.LOGGER.debug(cause.getMessage(), cause);
			}
			// 关闭通道, 并启动Inactive
			ctx.close().addListener(ExceptionListener.listener(this.ctx));
		}

		public Object invoke(Request request, Method method) throws Throwable {
			// 增加Token Header
			AckFuture future = new AckFuture(DefaultConnect.this.analyser, this, DefaultConnect.this.timeout, DefaultConnect.this.collector, this.ctx.channel().eventLoop(), method, DefaultConnect.this.token.set(request, this), DefaultConnect.this.profiles, DefaultConnect.this.quiet);
			ByteBuf buffer = DefaultConnect.this.encoder.encode(request.service(), request.method(), future.request());
			this.water4check();
			if (this.ctx.channel().eventLoop().inEventLoop()) {
				this.ctx.channel().attr(DefaultConnect.ACKS).get().put(future);
				this.ctx.writeAndFlush(buffer).addListener(ExceptionListener.listener(this.ctx));
			} else {
				this.ctx.channel().eventLoop().execute(new InvokeRunnable(this.ctx, future, buffer));
			}
			// 如果为Future或@Async则立即返回, 否则线程等待
			return future.request().async() ? future : future.get();
		}

		/**
		 * 回调, 唤醒线程
		 * 
		 * @param executor
		 * @param response
		 * @param acks
		 */
		private void response(Executor executor, Response response, AcksImpl acks) {
			AckFuture future = acks.get(response.ack());
			// 如获取不到ACK表示已超时
			if (future != null) {
				future.response(response);
			} else {
				DefaultConnect.LOGGER.warn("Missing ack for response: " + Arrays.toString(response.ack()) + " (" + this.remote.address() + "), may be timeout ...");
			}
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			AcksImpl acks = this.ctx.channel().attr(DefaultConnect.ACKS).get();
			ByteBuf buffer = ByteBuf.class.cast(message);
			if (DefaultConnect.EVENTLOOP_DECODE) {
				// 如果在EventLoop线程执行解码则立即执行
				this.response(ctx.channel().eventLoop(), Response.class.cast(DefaultConnect.this.decoder.decode(buffer)), acks);
			} else {
				DefaultConnect.this.threads.execute(new ResponseRunnable(this, ctx.channel().eventLoop(), buffer, acks));
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
					// 检查死连接
					channel.pipeline().addLast(new IdleStateHandler(DefaultConnect.IDLE_READ, DefaultConnect.IDLE_WRITE, DefaultConnect.IDLE_ALL));
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
			while (!DefaultConnect.this.shutdown) {
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

	private class InvokeRunnable implements Runnable {

		private final long created = System.currentTimeMillis();

		private final ChannelHandlerContext ctx;

		private final AckFuture future;

		private final ByteBuf buffer;

		private long running;

		private InvokeRunnable(ChannelHandlerContext ctx, AckFuture future, ByteBuf buffer) {
			super();
			this.future = future;
			this.buffer = buffer;
			this.ctx = ctx;
		}

		@Override
		public void run() {
			this.running = System.currentTimeMillis();
			// 线程等待提示
			if ((this.running - this.created) >= DefaultConnect.WAIT_WARN) {
				DefaultConnect.LOGGER.warn("[wait-warn][time=" + (this.running - this.created) + "][trace=" + this.future.request().get(Trace.TRACE) + "]");
			}
			this.ctx.channel().attr(DefaultConnect.ACKS).get().put(this.future);
			this.ctx.writeAndFlush(this.buffer).addListener(ExceptionListener.listener(this.ctx, this.future.request().get(Trace.TRACE)));
		}
	}

	private class ResponseRunnable implements Runnable {

		private final InvokerHandler invoker;

		private final Executor executor;

		private final ByteBuf buffer;

		private final AcksImpl acks;

		private ResponseRunnable(InvokerHandler invoker, Executor executor, ByteBuf buffer, AcksImpl acks) {
			super();
			this.executor = executor;
			this.invoker = invoker;
			this.buffer = buffer;
			this.acks = acks;
		}

		@Override
		public void run() {
			try {
				// 解析Response并回调
				Response response = Response.class.cast(DefaultConnect.this.decoder.decode(this.buffer));
				this.invoker.response(this.executor, response, this.acks);
			} catch (Throwable e) {
				DefaultConnect.LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private class AcksImpl implements Acks {

		private final Map<Bytes, AckFuture> waitings = new HashMap<Bytes, AckFuture>();

		public AckFuture put(AckFuture future) {
			this.waitings.put(new Bytes(future.request().ack()), future);
			return future.acks(this);
		}

		public AckFuture get(byte[] ack) {
			return this.waitings.get(new Bytes(ack));
		}

		public AckFuture remove(byte[] ack) {
			return this.waitings.remove(new Bytes(ack));
		}

		public String toString() {
			return "[acks=" + this.waitings.size() + "]";
		}
	}

	private class Bytes {

		private final byte[] bytes;

		private Bytes(byte[] bytes) {
			this.bytes = bytes;
		}

		@Override
		public boolean equals(Object obj) {
			// Guard case1, Null
			if (obj == null) {
				return false;
			}
			// Guard case2, 地址相等
			if (this == obj) {
				return true;
			}
			Bytes that = Bytes.class.cast(obj);
			return Arrays.equals(this.bytes, that.bytes);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(this.bytes);
		}
	}
}
