package com.kepler.connection.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Reject;
import com.kepler.connection.codec.CodecHeader;
import com.kepler.connection.codec.Decoder;
import com.kepler.connection.codec.Encoder;
import com.kepler.header.HeadersContext;
import com.kepler.host.impl.ServerHost;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestProcessor;
import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactories;
import com.kepler.quality.Quality;
import com.kepler.queue.QueueExecutor;
import com.kepler.queue.QueueRunnable;
import com.kepler.service.ExportedContext;
import com.kepler.token.TokenContext;
import com.kepler.trace.Trace;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

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
	 * 可写测试
	 */
	private static final int WRITE_WATERWATER_HIGH = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".write_water_high", 65536);

	private static final int WRITE_WATERWATER_LOW = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".write_water_low", 32768);

	private static final boolean WRITE_WATER = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".write_water", false);

	/**
	 * 黏包最大长度
	 */
	private static final int FRAGEMENT = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".fragment", Integer.MAX_VALUE);

	/**
	 * 服务绑定的本地IP
	 */
	private static final String BINDING = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".binding", "0.0.0.0");

	/**
	 * 等待预警
	 */
	private static final int WAIT_WARN = PropertiesUtils.get(DefaultServer.class.getName().toLowerCase() + ".wait_warn", 50);

	private static final Log LOGGER = LogFactory.getLog(DefaultServer.class);

	private final LengthFieldPrepender handler_length = new LengthFieldPrepender(CodecHeader.DEFAULT);

	private final ResourceHandler handler_resource = new ResourceHandler();

	private final ExportedHandler handler_export = new ExportedHandler();

	private final InitializerFactory inits = new InitializerFactory();

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final ThreadPoolExecutor threads;

	private final ResponseFactories response;

	private final RequestProcessor processor;

	private final ExportedContext exported;

	private final HeadersContext headers;

	private final QueueExecutor queue;

	private final TokenContext token;

	private final ServerHost local;

	private final Quality quality;

	private final Encoder encoder;

	private final Decoder decoder;

	private final Reject reject;

	private final Trace trace;

	public DefaultServer(Trace trace, Reject reject, Encoder encoder, Decoder decoder, Quality quality, ServerHost local, TokenContext token, QueueExecutor queue, ExportedContext exported, ResponseFactories response, HeadersContext headers, ThreadPoolExecutor threads, RequestProcessor processor) {
		super();
		this.processor = processor;
		this.exported = exported;
		this.response = response;
		this.quality = quality;
		this.threads = threads;
		this.headers = headers;
		this.encoder = encoder;
		this.decoder = decoder;
		this.reject = reject;
		this.token = token;
		this.trace = trace;
		this.local = local;
		this.queue = queue;
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		// 服务配置(绑定端口,SO_REUSEADDR=true)
		this.bootstrap.group(new NioEventLoopGroup(DefaultServer.EVENTLOOP_PARENT), new NioEventLoopGroup(DefaultServer.EVENTLOOP_CHILD)).channelFactory(DefaultChannelFactory.INSTANCE_SERVER).childHandler(this.inits.factory()).option(ChannelOption.SO_REUSEADDR, true).bind(DefaultServer.BINDING, this.local.port()).sync();
		DefaultServer.LOGGER.info("Server " + this.local + " started ... ");
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		this.bootstrap.group().shutdownGracefully().sync();
		this.bootstrap.childGroup().shutdownGracefully().sync();
		DefaultServer.LOGGER.warn("Server shutdown ... ");
	}

	private class InitializerFactory {

		public ChannelInitializer<SocketChannel> factory() {
			return new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.config().setSendBufferSize(DefaultServer.BUFFER_SEND);
					channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
					channel.config().setReceiveBufferSize(DefaultServer.BUFFER_RECV);
					// 检查死连接
					channel.pipeline().addLast(new IdleStateHandler(DefaultServer.IDLE_READ, DefaultServer.IDLE_WRITE, DefaultServer.IDLE_ALL));
					channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(DefaultServer.FRAGEMENT, 0, CodecHeader.DEFAULT, 0, CodecHeader.DEFAULT));
					// 连接控制
					channel.pipeline().addLast(DefaultServer.this.handler_resource);
					// 黏包
					channel.pipeline().addLast(DefaultServer.this.handler_length);
					// 本地服务
					channel.pipeline().addLast(DefaultServer.this.handler_export);
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

		/**
		 * 通道写入水位配置
		 * 
		 * @param ctx
		 */
		private void water4config(ChannelHandlerContext ctx) {
			if (DefaultServer.WRITE_WATER) {
				ctx.channel().config().setWriteBufferLowWaterMark(DefaultServer.WRITE_WATERWATER_LOW);
				ctx.channel().config().setWriteBufferHighWaterMark(DefaultServer.WRITE_WATERWATER_HIGH);
			}
		}

		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultServer.LOGGER.info("Connect active (" + ctx.channel().localAddress().toString() + " to " + ctx.channel().remoteAddress().toString() + ") ...");
			this.water4config(ctx);
			ctx.fireChannelActive();
		}

		/**
		 * @see com.kepler.host.ServerHost扫描端口时会触发对本地服务端口嗅探
		 */
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			DefaultServer.LOGGER.info("Connect inactive (" + ctx.channel().localAddress().toString() + " to " + ctx.channel().remoteAddress().toString() + ") ...");
			ctx.fireChannelInactive();
		}

		// 任何未捕获异常(如OOM)均需要终止通道
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (KeplerException.class.isAssignableFrom(cause.getClass())) {
				DefaultServer.LOGGER.error(cause.getMessage(), cause);
			} else {
				DefaultServer.LOGGER.debug(cause.getMessage(), cause);
			}
			// 关闭通道, 并启动Inactive
			ctx.close().addListener(ExceptionListener.listener(ctx));
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			DefaultServer.this.threads.execute(new Reply(ctx, ByteBuf.class.cast(message)));
		}

		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (DefaultServer.IDLE_CLOSE && evt instanceof IdleStateEvent) {
				DefaultServer.LOGGER.warn("Idle (" + IdleStateEvent.class.cast(evt).state() + ") connection closed. [local=" + ctx.channel().localAddress() + "][remote=" + ctx.channel().remoteAddress() + "]");
				DefaultServer.this.quality.idle();
				ctx.close().addListener(ExceptionListener.listener(ctx));
			}
		}

		private class Reply implements Runnable, QueueRunnable {

			/**
			 * Reply创建时间
			 */
			private final long created = System.currentTimeMillis();

			private final ChannelHandlerContext ctx;

			private final ByteBuf buffer;

			private Response response;

			private Request request;

			/**
			 * Reply执行时间
			 */
			private long running;

			/**
			 * Reply等待时间
			 */
			private long waiting;

			private Reply(ChannelHandlerContext ctx, ByteBuf buffer) {
				super();
				this.ctx = ctx;
				this.buffer = buffer;
			}

			/**
			 * 初始化
			 * 
			 * @return
			 */
			private Reply init() throws Exception {
				// Reply执行时间
				this.running = System.currentTimeMillis();
				this.waiting = this.running - this.created;
				if (this.waiting >= DefaultServer.WAIT_WARN) {
					DefaultServer.LOGGER.warn("[wait-warn][time=" + this.waiting + "][remote=" + this.ctx.channel().remoteAddress() + "]");
				}
				// 记录等待时间
				DefaultServer.this.quality.waiting(this.waiting);
				return this;
			}

			private Reply valid() throws Exception {
				// 校验是否Reject
				DefaultServer.this.reject.reject(this.request, this.ctx.channel().remoteAddress());
				// 校验请求合法性
				DefaultServer.this.token.valid(this.request);
				return this;
			}

			private Reply request() throws Exception {
				// 解析Request
				this.request = DefaultServer.this.processor.process(Request.class.cast(DefaultServer.this.decoder.decode(this.buffer)));
				return this;
			}

			private Reply response() throws Exception {
				try {
					// 线程Copy Header, 用于嵌套服务调用时传递
					DefaultServer.this.headers.set(this.request.headers());
					// 获取服务并执行
					this.response = DefaultServer.this.response.factory(this.request.serial()).response(this.request.ack(), DefaultServer.this.exported.get(this.request.service()).invoke(this.request, null), this.request.serial());
					return this;
				} catch (Throwable e) {
					this.response = DefaultServer.this.response.factory(this.request.serial()).throwable(this.request.ack(), e, this.request.serial());
					return this;
				} finally {
					// 删除Header避免同线程的其他业务复用
					DefaultServer.this.headers.release();
				}
			}

			private Reply write4trace() throws Exception {
				this.ctx.writeAndFlush(DefaultServer.this.encoder.encode(this.request.service(), this.request.method(), this.response)).addListener(ExceptionListener.listener(this.ctx, this.request.get(Trace.TRACE)));
				// 记录调用栈 (使用原始Request)
				DefaultServer.this.trace.trace(this.request, this.response, this.ctx.channel().localAddress().toString(), this.ctx.channel().remoteAddress().toString(), this.waiting, System.currentTimeMillis() - this.running, this.created);
				return this;
			}

			@Override
			public void run() {
				try {
					// 初始化, 记录时间信息
					this.init().request().valid();
					if (!DefaultServer.this.queue.executor(this.request, this)) {
						this.running();
					}
				} catch (Throwable throwable) {
					DefaultServer.LOGGER.error(throwable.getMessage(), throwable);
				}
			}

			@Override
			public void running() {
				try {
					this.response().write4trace();
				} catch (Throwable throwable) {
					DefaultServer.LOGGER.error(throwable.getMessage(), throwable);
				}
			}
		}
	}
}
