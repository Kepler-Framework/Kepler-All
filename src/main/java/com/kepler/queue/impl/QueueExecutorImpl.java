package com.kepler.queue.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.annotation.Queue;
import com.kepler.config.PropertiesUtils;
import com.kepler.protocol.Request;
import com.kepler.queue.QueueExecutor;
import com.kepler.queue.QueueRegister;
import com.kepler.queue.QueueRunnable;
import com.kepler.service.Service;
import com.kepler.thread.ThreadShutdown;

/**
 * @author KimShen
 *
 */
public class QueueExecutorImpl extends ThreadShutdown implements QueueExecutor, QueueRegister {

	private static final int THREAD_CORE = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".core", Math.max(Runtime.getRuntime().availableProcessors() * 2, 16));

	private static final int THREAD_MAX = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".max", QueueExecutorImpl.THREAD_CORE * 2);

	private static final int THREAD_KEEPALIVE = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".keepalive", 60000);

	private static final int THREAD_QUEUE = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".queue", 50);

	/**
	 * 是否使用ShutdownNow
	 */
	private static final boolean SHUTDOWN_WAITING = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".shutdown_waiting", false);

	/**
	 * 扫描线程池是否完毕间隔
	 */
	private static final int SHUTDOWN_INTERVAL = PropertiesUtils.get(QueueExecutorImpl.class.getName().toLowerCase() + ".shutdown_interval", 1000);

	private static final Log LOGGER = LogFactory.getLog(QueueExecutorImpl.class);

	private final Map<Service, ExecutorService> executors = new HashMap<Service, ExecutorService>();

	public void destroy() throws Exception {
		for (Service each : this.executors.keySet()) {
			this.destroy(this.executors.get(each));
		}
	}

	@Override
	public void register(Service service, Queue queue) {
		if (queue != null) {
			int size = queue.queue() != 0 ? queue.queue() : QueueExecutorImpl.THREAD_QUEUE;
			int core = queue.core() != 0 ? queue.core() : QueueExecutorImpl.THREAD_CORE;
			int max = queue.max() != 0 ? queue.max() : QueueExecutorImpl.THREAD_MAX;
			this.executors.put(service, new ThreadPoolExecutor(core, max, QueueExecutorImpl.THREAD_KEEPALIVE, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(size), new ThreadPoolExecutor.AbortPolicy()));
			QueueExecutorImpl.LOGGER.info("[register][queue=" + queue + "]" + service);
		}
	}

	@Override
	public boolean executor(Request request, QueueRunnable runnable) {
		ExecutorService executor = this.executors.get(request.service());
		if (executor != null) {
			executor.execute(new ProxyRunnable(runnable));
			return true;
		}
		return false;
	}

	@Override
	protected boolean waiting() {
		return QueueExecutorImpl.SHUTDOWN_WAITING;
	}

	@Override
	protected int interval() {
		return QueueExecutorImpl.SHUTDOWN_INTERVAL;
	}

	private class ProxyRunnable implements Runnable {

		private final QueueRunnable runnable;

		private ProxyRunnable(QueueRunnable runnable) {
			super();
			this.runnable = runnable;
		}

		@Override
		public void run() {
			this.runnable.running();
		}
	}
}
