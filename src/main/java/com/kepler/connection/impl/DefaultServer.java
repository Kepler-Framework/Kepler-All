package com.kepler.connection.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.handler.CodecHeader;
import com.kepler.connection.handler.DecoderHandler;
import com.kepler.connection.handler.EncoderHandler;
import com.kepler.connection.handler.ResourceHandler;
import com.kepler.header.HeadersContext;
import com.kepler.host.Host;
import com.kepler.host.impl.ServerHost;
import com.kepler.promotion.Promotion;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;
import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactory;
import com.kepler.serial.Serials;
import com.kepler.service.ExportedContext;
import com.kepler.token.TokenContext;
import com.kepler.trace.Trace;
import com.kepler.traffic.Traffic;

/**
 * @author kim 2015年7月8日
 */
public class DefaultServer {
	
	private static final int EVENTLOOP_PARENT = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".eventloop_parent", 1);

	private static final int EVENTLOOP_CHILD = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".eventloop_child", Runtime.getRuntime().availableProcessors() * 2);

	private static final int BUFFER_SEND = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".buffer_send", Integer.MAX_VALUE);

	private static final int BUFFER_RECV = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".buffer_recv", Integer.MAX_VALUE);

	private static final boolean IDLE_CLOSE = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".idle_close", true);

	private static final short IDLE_ALL = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".idle_all", Short.MAX_VALUE);

	private static final short IDLE_READ = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".idle_read", Short.MAX_VALUE);

	private static final short IDLE_WRITE = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".idle_write", Short.MAX_VALUE);

	/**
	 * 黏包最大长度
	 */
	private static final int FRAGEMENT = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".fragment", Integer.MAX_VALUE);

	/**
	 * 服务绑定的本地IP
	 */
	private static final String BINDING = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".binding", "0.0.0.0");

	private static final DefaultChannelFactory<ServerChannel> FACTORY = new DefaultChannelFactory<ServerChannel>(NioServerSocketChannel.class);

	private static final Log LOGGER = LogFactory.getLog(DefaultServer.class);

	private final InitializerFactory inits = new InitializerFactory();

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final RequestValidation validation;

	private final ThreadPoolExecutor threads;

	private final ExportedContext exported;

	private final ResponseFactory response;

	private final HeadersContext headers;

	private final Promotion promotion;

	private final TokenContext token;

	private final ServerHost local;

	private final Serials serials;

	private final Traffic traffic;

	private final Trace trace;

	public DefaultServer(Trace trace, ServerHost local, Traffic traffic, Serials serials, Promotion promotion, TokenContext token, ExportedContext exported, ResponseFactory response, HeadersContext headers, ThreadPoolExecutor threads, RequestValidation validation) {
		super();
		this.validation = validation;
		this.promotion = promotion;
		this.exported = exported;
		this.response = response;
		this.threads = threads;
		this.headers = headers;
		this.traffic = traffic;
		this.serials = serials;
		this.token = token;
		this.trace = trace;
		this.local = local;
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		// 连接控制
		this.inits.add(new ResourceHandler());
		// 黏包
		this.inits.add(new LengthFieldPrepender(CodecHeader.DEFAULT));
		// 编码/解码
		this.inits.add(new EncoderHandler(DefaultServer.this.traffic, DefaultServer.this.serials, Response.class));
		this.inits.add(new DecoderHandler(DefaultServer.this.traffic, DefaultServer.this.serials, Request.class));
		// 本地服务
		this.inits.add(new ExportedHandler());
		// 服务配置(绑定端口,SO_REUSEADDR=true)
		this.bootstrap.group(new NioEventLoopGroup(DefaultServer.EVENTLOOP_PARENT), new NioEventLoopGroup(DefaultServer.EVENTLOOP_CHILD)).channelFactory(DefaultServer.FACTORY).childHandler(this.inits.factory()).option(ChannelOption.SO_REUSEADDR, true).bind(DefaultServer.BINDING, this.local.port()).sync();
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		this.bootstrap.group().shutdownGracefully().sync();
		DefaultServer.LOGGER.warn("Server shutdown ... ");
	}

	private class InitializerFactory {

		private final List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();

		public void add(ChannelHandler handler) {
			this.handlers.add(handler);
		}

		public ChannelInitializer<SocketChannel> factory() {
			return new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.config().setReceiveBufferSize(DefaultServer.BUFFER_RECV);
					channel.config().setSendBufferSize(DefaultServer.BUFFER_SEND);
					channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
					// 检查死连接
					channel.pipeline().addLast(new IdleStateHandler(DefaultServer.IDLE_READ, DefaultServer.IDLE_WRITE, DefaultServer.IDLE_ALL));
					channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(DefaultServer.FRAGEMENT, 0, CodecHeader.DEFAULT, 0, CodecHeader.DEFAULT));
					for (ChannelHandler each : InitializerFactory.this.handlers) {
						channel.pipeline().addLast(each);
					}
				}
			};
		}
	}

	/**
	 * @author kim
	 *
	 * 2016年2月17日
	 */
	@Sharable
	private class ExportedHandler extends ChannelInboundHandlerAdapter {

		private String target;

		private String local;

		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			this.local = ctx.channel().localAddress().toString();
			this.target = ctx.channel().remoteAddress().toString();
			DefaultServer.LOGGER.info("Connect active (" + this.local + " to " + this.target + ") ...");
			ctx.fireChannelActive();
		}

		/**
		 * @see com.kepler.host.ServerHost扫描端口时会触发对本地服务端口嗅探
		 */
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			DefaultServer.LOGGER.info("Connect inactive (" + this.local + " to " + this.target + ") ...");
			ctx.fireChannelInactive();
		}

		// 任何未捕获异常(如OOM)均需要终止通道
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
				DefaultServer.LOGGER.error(cause.getMessage(), cause);
			} else {
				DefaultServer.LOGGER.debug(cause.getMessage(), cause);
			}
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			Reply reply = new Reply(ctx, Request.class.cast(message));
			// 使用EventLoop线程还是使用Kepler线程
			if (DefaultServer.this.promotion.promote(reply.request())) {
				ctx.executor().execute(reply);
			} else {
				DefaultServer.this.threads.execute(reply);
			}
		}

		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (DefaultServer.IDLE_CLOSE && evt instanceof IdleStateEvent) {
				DefaultServer.LOGGER.warn("Idle (" + IdleStateEvent.class.cast(evt).state() + ") trigger close ... ");
				ctx.close().addListener(ExceptionListener.TRACE);
			}
		}

		private class Reply implements Runnable {

			/**
			 * Reply创建时间
			 */
			private final long created = System.currentTimeMillis();

			private final ChannelHandlerContext ctx;

			private final Request request;

			/**
			 * Reply执行时间
			 */
			private long running;

			public Reply(ChannelHandlerContext ctx, Request request) {
				super();
				this.ctx = ctx;
				this.request = request;
			}

			private Reply init() {
				// Reply执行时间
				this.running = System.currentTimeMillis();
				// 线程Copy Header, 用于嵌套服务调用时传递(In Kepler Threads)
				DefaultServer.this.headers.set(this.request.headers());
				return this;
			}

			@Override
			public void run() {
				Response response = this.init().response(this.request);
				this.ctx.writeAndFlush(response).addListener(ExceptionListener.TRACE);
				// 记录调用栈
				DefaultServer.this.trace.trace(this.request, response, ExportedHandler.this.local, ExportedHandler.this.target, this.running - this.created, System.currentTimeMillis() - this.running);
			}

			public Request request() {
				return this.request;
			}

			private Response response(Request request) {
				try {
					// DefaultServer.this.token.valid(request, Host.TAG_VAL) Token校验
					// 校验Request合法性(如JSR 303)
					DefaultServer.this.validation.valid(DefaultServer.this.token.valid(request, Host.TAG_VAL));
					// 获取服务并执行
					return DefaultServer.this.response.response(request.ack(), DefaultServer.this.exported.get(request.service()).invoke(request), request.serial());
				} catch (Throwable e) {
					// 业务异常
					DefaultServer.LOGGER.error(e.getMessage(), e);
					return DefaultServer.this.response.throwable(request.ack(), e, request.serial());
				} finally {
					DefaultServer.this.promotion.record(request, this.running);
				}
			}
		}
	}
}
