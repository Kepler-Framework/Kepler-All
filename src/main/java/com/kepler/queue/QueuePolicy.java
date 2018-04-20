package com.kepler.queue;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author KimShen
 *
 */
public enum QueuePolicy {

	ABORT(0), DISCARD(1), CALLERRUNS(2), DISCARDOLDEST(3);

	private final RejectedExecutionHandler handler;

	private QueuePolicy(int code) {
		switch (code) {
		case 0: {
			this.handler = new ThreadPoolExecutor.AbortPolicy();
			return;
		}
		case 1: {
			this.handler = new ThreadPoolExecutor.DiscardPolicy();
			return;
		}
		case 2: {
			this.handler = new ThreadPoolExecutor.CallerRunsPolicy();
			return;
		}
		case 3: {
			this.handler = new ThreadPoolExecutor.DiscardOldestPolicy();
			return;
		}
		default: {
			this.handler = new ThreadPoolExecutor.AbortPolicy();
			return;
		}
		}
	}

	public RejectedExecutionHandler handler() {
		return this.handler;
	}
}
