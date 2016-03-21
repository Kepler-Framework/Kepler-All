package com.kepler.connection.impl;

import java.util.concurrent.atomic.AtomicLong;

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
		this.counter.incrementAndGet();
	}

	@Override
	public void decr() {
		this.counter.decrementAndGet();
	}

	@Override
	public long remain() {
		return this.counter.get();
	}
}
