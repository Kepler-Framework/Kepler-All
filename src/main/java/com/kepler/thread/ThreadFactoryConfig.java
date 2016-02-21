package com.kepler.thread;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;

/**
 * @author kim 2016年1月4日 
 * 
 * ThreadFactory隐性依赖ThreadFactory -> DefaultConnect -> DefaultImportedListener -> ZkContext -> ChainedExported -> ExportedDiscovery, 无法被DefaultConfig加载.使用此代理类
 * 
 */
@Internal
public class ThreadFactoryConfig {

	private final ThreadPoolExecutor threads;

	public ThreadFactoryConfig(ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
	}

	@Config(value = "com.kepler.thread.threadfactory.max")
	public void max(int size) {
		this.threads.setMaximumPoolSize(size);
	}

	@Config(value = "com.kepler.thread.threadfactory.core")
	public void core(int size) {
		this.threads.setCorePoolSize(size);
	}

	@Config(value = "com.kepler.thread.threadfactory.keepalive")
	public void keepalive(long keepalive) {
		this.threads.setKeepAliveTime(keepalive, TimeUnit.MILLISECONDS);
	}
}
