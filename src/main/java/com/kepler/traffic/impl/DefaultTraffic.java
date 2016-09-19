package com.kepler.traffic.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.traffic.Traffic;

/**
 * @author kim 2016年1月7日
 */
public class DefaultTraffic implements Traffic {

	/**
	 * 出站数据大小
	 */
	private final AtomicLong output = new AtomicLong();

	/**
	 * 入站数据大小
	 */
	private final AtomicLong input = new AtomicLong();

	@Override
	public void input(long bytes) {
		if (StatusTask.ENABLED) {
			this.input.addAndGet(bytes);
		}
	}

	@Override
	public void output(long bytes) {
		if (StatusTask.ENABLED) {
			this.output.addAndGet(bytes);
		}
	}

	public long getInputAndReset() {
		return StatusTask.ENABLED ? this.input.getAndSet(0) : 0L;
	}

	public long getOutputAndReset() {
		return StatusTask.ENABLED ? this.output.getAndSet(0) : 0L;
	}
}
