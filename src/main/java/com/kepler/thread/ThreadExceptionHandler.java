package com.kepler.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kim 2016年1月6日
 */
public class ThreadExceptionHandler implements UncaughtExceptionHandler {

	private static final Log LOGGER = LogFactory.getLog(ThreadExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ThreadExceptionHandler.LOGGER.error("Thread " + t.getId() + ": " + e.getMessage(), e);
	}

	public void init() {
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
}
