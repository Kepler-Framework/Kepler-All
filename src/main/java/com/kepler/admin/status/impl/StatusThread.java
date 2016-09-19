package com.kepler.admin.status.impl;

import java.util.concurrent.ThreadPoolExecutor;

import com.kepler.admin.status.Refresh;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年8月11日
 */
public class StatusThread extends StatusDynamic implements Refresh {

	// 允许收集的最大数量(每个周期)
	private static final byte MAX = PropertiesUtils.get(StatusThread.class.getName().toLowerCase() + ".max", (byte) 10);

	private final ThreadPoolExecutor executor;

	public StatusThread(ThreadPoolExecutor executor) {
		super(new String[] { "thread_active_jvm", "thread_active_framework" });
		this.executor = executor;
	}

	@Override
	public void refresh() {
		// 当前时间
		long current = System.currentTimeMillis();
		super.add("thread_active_jvm", current, Thread.activeCount());
		super.add("thread_active_framework", current, this.executor.getActiveCount());
	}

	@Override
	protected byte max() {
		return StatusThread.MAX;
	}
}
