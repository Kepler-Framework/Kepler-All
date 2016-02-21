package com.kepler.connection.impl;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kim 2015年7月8日
 */
public class ExceptionListener implements GenericFutureListener<Future<Void>> {

	public final static ExceptionListener TRACE = new ExceptionListener();

	private final static Log LOGGER = LogFactory.getLog(ExceptionListener.class);

	private ExceptionListener() {

	}

	@Override
	public void operationComplete(Future<Void> future) throws Exception {
		if (!future.isSuccess() && future.cause() != null) {
			ExceptionListener.LOGGER.error(future.cause().getMessage(), future.cause());
		}
	}
}