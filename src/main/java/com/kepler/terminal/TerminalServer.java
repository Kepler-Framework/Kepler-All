package com.kepler.terminal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerLocalException;
import com.kepler.config.Config;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.impl.ServerHost;
import com.kepler.serial.Serials;
import com.kepler.zookeeper.ZkClient;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TerminalServer {

	private static final Log LOGGER = LogFactory.getLog(TerminalServer.class);

	public static final String LOOP = "127.0.0.1";

	/**
	 * Terminal 服务开关
	 */
	private static final boolean ENABLED = PropertiesUtils.get(TerminalServer.class.getName().toLowerCase() + ".enabled", true);
	/**
	 * 本地默认端口
	 */
	private static final int PORT = PropertiesUtils.get(TerminalServer.class.getName().toLowerCase() + ".port", 8888);

	/**
	 * 本地端口嗅探范围
	 */
	private final static int RANGE = PropertiesUtils.get(TerminalServer.class.getName().toLowerCase() + ".range", 1000);

	/**
	 * 本地端口嗅探间隔
	 */
	private final static int INTERVAL = PropertiesUtils.get(TerminalServer.class.getName().toLowerCase() + ".interval", 500);

	/**
	 * 退出命令
	 */
	private static final String CMD_QUIT = "quit";

	/**
	 * 命令最大长度
	 */
	private static final int CMD_MAX_LENGTH = PropertiesUtils.get(TerminalServer.class.getName().toLowerCase() + ".cmd_max_length", 1024);

	/**
	 * 命令格式
	 */
	private static final Pattern CMD_PATTERN = Pattern.compile("(set)[ ]+([^=,^ ]+)=([^=,^ ]+)");

	private static final String USAGE = "Please input the command: set key=value\ntype 'quit' to exit\n";

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final ServerHost local;

	private final Serials serials;

	private final ZkClient zoo;

	private final Config config;

	public TerminalServer(Serials serials, ServerHost local, ZkClient zoo, Config config) {
		this.serials = serials;
		this.local = local;
		this.zoo = zoo;
		this.config = config;
	}

	public void init() throws Exception {
		if (!TerminalServer.ENABLED) {
			TerminalServer.LOGGER.warn("TerminalServer is not enabled!");
			return;
		}
		this.bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(1)).channel(NioServerSocketChannel.class)
		        .childHandler(new ChannelInitializer<SocketChannel>() {
			        @Override
			        public void initChannel(SocketChannel ch) throws Exception {
				        ch.pipeline().addLast(new LineBasedFrameDecoder(TerminalServer.CMD_MAX_LENGTH, true, true))
				                .addLast(new StringDecoder()).addLast(new StringEncoder()).addLast(new ConfigHandler());
			        }
		        }).option(ChannelOption.SO_REUSEADDR, true).bind(TerminalServer.LOOP, this.availablePort()).sync();

	}

	public void destroy() throws Exception {
		if (!TerminalServer.ENABLED) {
			return;
		}
		this.bootstrap.group().shutdownGracefully().sync();
		TerminalServer.LOGGER.warn("Terminal Server shutdown ... ");
	}

	private int availablePort() throws Exception {
		for (int index = TerminalServer.PORT; index < TerminalServer.PORT + TerminalServer.RANGE; index++) {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(InetAddress.getByName(TerminalServer.LOOP), index),
				        TerminalServer.INTERVAL);
			} catch (IOException e) {
				TerminalServer.LOGGER.info("Port " + index + " used for terminal server ");
				return index;
			}
		}
		throw new KeplerLocalException("Cannot allocate port for terminal server");
	}

	private class ConfigHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			String cmdLine = String.class.cast(msg);
			System.out.println(cmdLine);
			TerminalServer.LOGGER.info("Recieve the command:" + cmdLine);
			if (TerminalServer.CMD_QUIT.equalsIgnoreCase(cmdLine)) {
				ctx.writeAndFlush("Exit...");
				ctx.close();
				return;
			}
			Command command = new Command(cmdLine);
			if (!command.valid()) {
				ctx.writeAndFlush(TerminalServer.USAGE);
			} else {
				command.execute();
				String response = "config updated!\n";
				ctx.writeAndFlush(response);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			TerminalServer.LOGGER.error(cause.getMessage(), cause);
			ctx.close();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			String local = ctx.channel().localAddress().toString();
			String target = ctx.channel().remoteAddress().toString();
			TerminalServer.LOGGER.info("ConfigHandler:Connect active (" + local + " to " + target + ") ...");
			ctx.writeAndFlush(TerminalServer.USAGE);
			ctx.fireChannelActive();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			String local = ctx.channel().localAddress().toString();
			String target = ctx.channel().remoteAddress().toString();
			TerminalServer.LOGGER.info("ConfigHandler:Connect inactive (" + local + " to " + target + ") ...");
			ctx.fireChannelInactive();
		}
	}

	private class Command {

		private String cmd;

		private String key;

		private String value;

		private boolean valid;

		private final Matcher matcher;

		public Command(String cmdLine) {
			this.matcher = TerminalServer.CMD_PATTERN.matcher(cmdLine);
			if (this.matcher.matches()) {
				this.valid = true;
				this.cmd = this.matcher.group(1);
				this.key = this.matcher.group(2);
				this.value = this.matcher.group(3);
			}
		}

		public void execute() {
			Map<String, String> configs = PropertiesUtils.properties();
			configs.put(this.key, this.value);
			TerminalServer.this.config.config(configs);
			// TODO update zk
		}

		public boolean valid() {
			return this.valid;
		}

	}
}
