package com.kepler.admin.status.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.kepler.admin.status.Status;

/**
 * @author kim 2015年8月11日
 */
public class Status4Thread implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	private final ThreadPoolExecutor threads;

	public Status4Thread(ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
	}

	@Override
	public Map<String, Object> get() {
		this.status.put("thread_active", Thread.activeCount());
		this.status.put("thread_stacks", Thread.getAllStackTraces().size());
		this.status.put("thread_framework_pool", this.threads.getPoolSize());
		this.status.put("thread_framework_core", this.threads.getCorePoolSize());
		this.status.put("thread_framework_active", this.threads.getActiveCount());
		this.status.put("thread_framework_largest", this.threads.getLargestPoolSize());
		this.status.put("thread_framework_maximum", this.threads.getMaximumPoolSize());
		this.status.put("thread_framework_keepalive", this.threads.getKeepAliveTime(TimeUnit.MILLISECONDS));
		this.status.put("thread_framework_task_count", this.threads.getTaskCount());
		this.status.put("thread_framework_task_completed", this.threads.getCompletedTaskCount());
		return this.status;
	}
}
