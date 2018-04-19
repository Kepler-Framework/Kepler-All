package com.kepler.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年7月16日
 */
public class ThreadFactory extends ThreadShutdown implements FactoryBean<ThreadPoolExecutor> {

	private static final int THREAD_CORE = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".core", Math.max(Runtime.getRuntime().availableProcessors() * 2, 16));

	private static final int THREAD_MAX = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".max", ThreadFactory.THREAD_CORE * 2);

	private static final int THREAD_KEEPALIVE = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".keepalive", 60000);

	private static final int THREAD_QUEUE = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".queue", 50);

	/**
	 * 是否使用ShutdownNow
	 */
	private static final boolean SHUTDOWN_WAITING = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".shutdown_waiting", false);

	/**
	 * 扫描线程池是否完毕间隔
	 */
	private static final int SHUTDOWN_INTERVAL = PropertiesUtils.get(ThreadFactory.class.getName().toLowerCase() + ".shutdown_interval", 1000);

	private static final Log LOGGER = LogFactory.getLog(ThreadFactory.class);

	private ThreadPoolExecutor threads;

	@Override
	public ThreadPoolExecutor getObject() throws Exception {
		return this.threads;
	}

	@Override
	public Class<ThreadPoolExecutor> getObjectType() {
		return ThreadPoolExecutor.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * For Spring
	 */
	public void init() {
		ThreadFactory.LOGGER.info("Thread max: " + ThreadFactory.THREAD_MAX);
		ThreadFactory.LOGGER.info("Thread core: " + ThreadFactory.THREAD_CORE);
		ThreadFactory.LOGGER.info("Thread queue: " + ThreadFactory.THREAD_QUEUE);
		this.threads = new ThreadPoolExecutor(ThreadFactory.THREAD_CORE, ThreadFactory.THREAD_MAX, ThreadFactory.THREAD_KEEPALIVE, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(ThreadFactory.THREAD_QUEUE), new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * For Spring
	 */
	protected void destroy() throws Exception {
		super.destroy(this.threads);
	}

	@Override
	protected boolean waiting() {
		return ThreadFactory.SHUTDOWN_WAITING;
	}

	@Override
	protected int interval() {
		return ThreadFactory.SHUTDOWN_INTERVAL;
	}
}
