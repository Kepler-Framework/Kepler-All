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
import com.kepler.config.Profile;
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

	private static final String THREAD_KEEPALIVE_KEY = QueueExecutorImpl.class.getName().toLowerCase() + ".keepalive";

	private static final String THREAD_QUEUE_KEY = QueueExecutorImpl.class.getName().toLowerCase() + ".queue";

	private static final String THREAD_CORE_KEY = QueueExecutorImpl.class.getName().toLowerCase() + ".core";

	private static final String THREAD_MAX_KEY = QueueExecutorImpl.class.getName().toLowerCase() + ".max";

	private static final int THREAD_CORE_DEF = PropertiesUtils.get(QueueExecutorImpl.THREAD_CORE_KEY, Math.max(Runtime.getRuntime().availableProcessors() * 2, 16));

	private static final int THREAD_MAX_DEF = PropertiesUtils.get(QueueExecutorImpl.THREAD_MAX_KEY, QueueExecutorImpl.THREAD_CORE_DEF * 2);

	private static final int THREAD_KEEPALIVE_DEF = PropertiesUtils.get(QueueExecutorImpl.THREAD_KEEPALIVE_KEY, 60000);

	private static final int THREAD_QUEUE_DEF = PropertiesUtils.get(QueueExecutorImpl.THREAD_QUEUE_KEY, 50);

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

	private final Profile profile;

	public QueueExecutorImpl(Profile profile) {
		super();
		this.profile = profile;
	}

	public void destroy() throws Exception {
		for (Service each : this.executors.keySet()) {
			this.destroy(this.executors.get(each));
		}
	}

	@Override
	public void register(Service service, Queue queue) {
		if (queue != null) {
			int size = queue.queue() != 0 ? queue.queue() : PropertiesUtils.profile(this.profile.profile(service), QueueExecutorImpl.THREAD_QUEUE_KEY, QueueExecutorImpl.THREAD_QUEUE_DEF);
			int core = queue.core() != 0 ? queue.core() : PropertiesUtils.profile(this.profile.profile(service), QueueExecutorImpl.THREAD_CORE_KEY, QueueExecutorImpl.THREAD_CORE_DEF);
			int max = queue.max() != 0 ? queue.max() : PropertiesUtils.profile(this.profile.profile(service), QueueExecutorImpl.THREAD_MAX_KEY, QueueExecutorImpl.THREAD_MAX_DEF);
			this.executors.put(service, new ThreadPoolExecutor(core, max, QueueExecutorImpl.THREAD_KEEPALIVE_DEF, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(size), new ThreadPoolExecutor.AbortPolicy()));
			QueueExecutorImpl.LOGGER.info("[register][service=" + service.service() + "][version=" + service.version() + "][catalog=" + service.catalog() + "][queue=" + queue + "][core=" + core + "][max=" + max + "]");
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
