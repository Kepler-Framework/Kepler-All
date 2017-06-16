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

	public static final Boolean DETAIL = PropertiesUtils.get(ExceptionListener.class.getName().toLowerCase() + ".detail", true);

	public static final ExceptionListener INSTANCE = new ExceptionListener();

	private static final Log LOGGER = LogFactory.getLog(ExceptionListener.class);

	private final ChannelHandlerContext context;

	private ExceptionListener() {
		this.context = null;
	}

	public ExceptionListener(ChannelHandlerContext context) {
		super();
		this.context = context;
	}

	@Override
	public void operationComplete(Future<Void> future) throws Exception {
		if (!future.isSuccess() && future.cause() != null) {
			// 如果存在Context则获取Remote
			String message = "[message=" + future.cause().getMessage() + "]" + (this.context != null ? "[remote=" + this.context.channel().remoteAddress() + "]" : "");
			ExceptionListener.LOGGER.error(message, future.cause());
		}
	}

	public static ExceptionListener listener(ChannelHandlerContext context) {
		return ExceptionListener.DETAIL ? new ExceptionListener(context) : ExceptionListener.INSTANCE;
	}
}