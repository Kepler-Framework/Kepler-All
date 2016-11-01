package com.kepler.quality.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.config.PropertiesUtils;
import com.kepler.quality.Quality;

/**
 * @author KimShen
 *
 */
public class DefaultQuality implements Quality, Runnable {

	/**
	 * 队列长度
	 */
	private static final int QUEUE_SIZE = PropertiesUtils.get(DefaultQuality.class.getName().toLowerCase() + ".queue_size", Short.MAX_VALUE);

	private static final int INTERVAL = PropertiesUtils.get(DefaultQuality.class.getName().toLowerCase() + ".interval", 60000);

	private static final Log LOGGER = LogFactory.getLog(DefaultQuality.class);

	/**
	 * 等待队列
	 */
	private final BlockingQueue<Long> waitings = new ArrayBlockingQueue<Long>(DefaultQuality.QUEUE_SIZE);

	/**
	 * 熔断
	 */
	private final AtomicLong breaking = new AtomicLong();

	/**
	 * 降级
	 */
	private final AtomicLong demoting = new AtomicLong();

	/**
	 * 闲置断开
	 */
	private final AtomicLong idle = new AtomicLong();

	private final ThreadPoolExecutor threads;

	volatile private boolean shutdown;

	/**
	 * 最大等待
	 */
	volatile private long waiting;

	public DefaultQuality(ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
	}

	/**
	 * For Spring
	 */
	public void init() {
		// 单线程操作
		this.threads.execute(this);
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		this.shutdown = true;
	}

	@Override
	public void idle() {
		if (StatusTask.ENABLED) {
			this.idle.incrementAndGet();
		}
	}

	@Override
	public void breaking() {
		if (StatusTask.ENABLED) {
			this.breaking.incrementAndGet();
		}
	}

	@Override
	public void demoting() {
		if (StatusTask.ENABLED) {
			this.demoting.incrementAndGet();
		}
	}

	@Override
	public void waiting(long waiting) {
		if (StatusTask.ENABLED) {
			if (!this.waitings.offer(waiting)) {
				// 插入失败提示
				DefaultQuality.LOGGER.warn("Collect waiting failed: " + waiting);
			}
		}
	}

	@Override
	public long getBreakingAndReset() {
		return StatusTask.ENABLED ? this.breaking.getAndSet(0) : 0;
	}

	@Override
	public long getDemotingAndReset() {
		return StatusTask.ENABLED ? this.demoting.getAndSet(0) : 0;
	}

	@Override
	public long getWaitingAndReset() {
		try {
			return StatusTask.ENABLED ? this.waiting : 0;
		} finally {
			this.waiting = 0;
		}
	}

	@Override
	public long getIdleAndReset() {
		return StatusTask.ENABLED ? this.idle.getAndSet(0) : 0;
	}

	@Override
	public void run() {
		while (!this.shutdown) {
			try {
				Long waiting = this.waitings.poll(DefaultQuality.INTERVAL, TimeUnit.MILLISECONDS);
				// 存在且大于当前等待
				if (waiting != null && (waiting > this.waiting)) {
					this.waiting = waiting;
				}
			} catch (Throwable e) {
				DefaultQuality.LOGGER.debug(e.getMessage(), e);
			}
		}
		DefaultQuality.LOGGER.warn("Quailty shutdown ... ");
	}
}
