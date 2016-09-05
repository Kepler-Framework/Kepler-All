package com.kepler.connection.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.connection.Counter;

/**
 * @author kim
 *
 * 2016年3月18日
 */
public class DefaultCounter implements Counter {

	private final AtomicLong counter = new AtomicLong();

	@Override
	public void incr() {
		// 开启统计时收集
		if (StatusTask.ENABLED) {
			this.counter.incrementAndGet();
		}
	}

	@Override
	public void decr() {
		if (StatusTask.ENABLED) {
			this.counter.decrementAndGet();
		}
	}

	@Override
	public long remain() {
		return this.counter.get();
	}
}
