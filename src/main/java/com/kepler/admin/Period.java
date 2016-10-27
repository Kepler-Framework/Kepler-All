package com.kepler.admin;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年7月22日
 */
public class Period implements Runnable {

	private static final int INTERVAL = PropertiesUtils.get(Period.class.getName().toLowerCase() + ".interval", 60000);

	private static final Log LOGGER = LogFactory.getLog(Period.class);

	private final BlockingQueue<PeriodTask> tasks = new DelayQueue<PeriodTask>();

	private final ThreadPoolExecutor threads;

	volatile private boolean shutdown;

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
		this.threads.execute(this);
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		this.shutdown = true;
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
		while (!this.shutdown) {
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
