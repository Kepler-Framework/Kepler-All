package com.kepler.traffic.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.traffic.Traffic;

/**
 * @author kim 2016年1月7日
 */
public class DefaultTraffic implements Traffic {

	private final AtomicLong output = new AtomicLong();

	private final AtomicLong input = new AtomicLong();

	@Override
	public void input(long bytes) {
		this.input.addAndGet(bytes);
	}

	@Override
	public void output(long bytes) {
		this.output.addAndGet(bytes);
	}

	public long getInputAndReset() {
		return this.input.getAndSet(0);
	}

	public long getOutputAndReset() {
		return this.output.getAndSet(0);
	}
}
