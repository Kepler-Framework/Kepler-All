package com.kepler.connection.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author kim 2015年7月8日
 */
public class ExceptionListener implements GenericFutureListener<Future<Void>> {

	/**
	 * 等待预警
	 */
	private static final int WAIT_WARN = PropertiesUtils.get(ExceptionListener.class.getName().toLowerCase() + ".wait_warn", 50);

	public static final Boolean DETAIL = PropertiesUtils.get(ExceptionListener.class.getName().toLowerCase() + ".detail", true);

	private static final Log LOGGER = LogFactory.getLog(ExceptionListener.class);

	public static final ExceptionListener INSTANCE = new ExceptionListener();

	private final long created = System.currentTimeMillis();

	private final ChannelHandlerContext context;

	private final String trace;

	private long running;

	private ExceptionListener() {
		this.context = null;
		this.trace = null;
	}

	public ExceptionListener(ChannelHandlerContext context, String trace) {
		super();
		this.context = context;
		this.trace = trace;
	}

	@Override
	public void operationComplete(Future<Void> future) throws Exception {
		if (ExceptionListener.DETAIL) {
			this.running = System.currentTimeMillis();
			if ((running - this.created) >= ExceptionListener.WAIT_WARN) {
				ExceptionListener.LOGGER.warn("[wait-warn][time=" + (this.running - this.created) + "][trace=" + this.trace + "]");
			}
		}
		if (!future.isSuccess() && future.cause() != null) {
			// 如果存在Context则获取Remote
			String message = "[message=" + future.cause().getMessage() + "]" + (this.context != null ? "[remote=" + this.context.channel().remoteAddress() + "]" : "");
			ExceptionListener.LOGGER.error(message, future.cause());
		}
	}

	public static ExceptionListener listener(ChannelHandlerContext context, String trace) {
		return ExceptionListener.DETAIL ? new ExceptionListener(context, trace) : ExceptionListener.INSTANCE;
	}

	public static ExceptionListener listener(ChannelHandlerContext context) {
		return ExceptionListener.listener(context, null);
	}
}