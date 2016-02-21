package com.kepler.invoker.async;

import com.kepler.thread.FutureDelegate;

/**
 * @author kim
 *
 * 2016年2月15日
 */
class AsyncDelegate {

	/**
	 * 实际代理Future
	 */
	private FutureDelegate delegate;

	private boolean blocking;

	/**
	 * @param delegate
	 * @param blocking 原调用是否等待结果
	 */
	AsyncDelegate(FutureDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * 修改blocking策略
	 * 
	 * @param blocking
	 * @return
	 */
	AsyncDelegate blocking(boolean blocking) {
		this.blocking = blocking;
		return this;
	}

	/**
	 * 原调用是否等待结果
	 * 
	 * @return
	 */
	boolean blocking() {
		return this.blocking;
	}

	FutureDelegate future() {
		return this.delegate;
	}
}
