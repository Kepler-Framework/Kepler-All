package com.kepler.management;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年7月22日
 */
public class Period implements Runnable {

	private static final int INTERVAL = PropertiesUtils.get(Period.class.getName().toLowerCase() + ".interval", 60000);

	/**
	 * Period线程数量
	 */
	private static final int THREAD = PropertiesUtils.get(Period.class.getName().toLowerCase() + ".thread", 1);

	private static final Log LOGGER = LogFactory.getLog(Period.class);

	private final BlockingQueue<PeriodTask> tasks = new DelayQueue<PeriodTask>();

	private final AtomicBoolean shutdown = new AtomicBoolean();

	private final ThreadPoolExecutor threads;

	public Period(ThreadPoolExecutor threads, List<PeriodTask> tasks) {
		super();
		this.threads = threads;
		// 初始化PeriodTask
		for (PeriodTask task : tasks) {
			this.tasks.offer(task.prepare());
		}
	}

	/**
	 * For Spring
	 */
	public void init() {
		for (int index = 0; index < Period.THREAD; index++) {
			this.threads.execute(this);
		}
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		this.shutdown.set(true);
	}

	private void command(PeriodTask task) {
		try {
			task.command();
		} finally {
			// 重置Task并放入队列
			this.tasks.add(task.prepare());
		}
	}

	@Override
	public void run() {
		while (!this.shutdown.get()) {
			try {
				PeriodTask task = this.tasks.poll(Period.INTERVAL, TimeUnit.MILLISECONDS);
				if (task != null) {
					this.command(task);
				}
			} catch (Throwable e) {
				Period.LOGGER.debug(e.getMessage(), e);
			}
		}
		Period.LOGGER.warn("Period shutdown ... ");
	}
}
