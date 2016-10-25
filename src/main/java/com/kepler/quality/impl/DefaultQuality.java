package com.kepler.quality.impl;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.config.PropertiesUtils;
import com.kepler.quality.Quality;

/**
 * @author KimShen
 *
 */
public class DefaultQuality implements Quality {

	private static final int TIMES = PropertiesUtils.get(DefaultQuality.class.getName().toLowerCase() + ".times", Byte.MAX_VALUE);

	private static final Log LOGGER = LogFactory.getLog(DefaultQuality.class);

	private final AtomicReference<Long> waiting = new AtomicReference<Long>();

	private final AtomicLong breaking = new AtomicLong();

	private final AtomicLong demoting = new AtomicLong();

	private final AtomicLong idle = new AtomicLong();

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
			for (int index = 0; index < DefaultQuality.TIMES; index++) {
				long current = this.waiting.get();
				if (current < waiting) {
					// CAS成功则返回. 失败则继续循环
					if (this.waiting.compareAndSet(current, waiting)) {
						return;
					}
				} else {
					return;
				}
			}
			// 尝试TIMES失败后提示日志
			DefaultQuality.LOGGER.warn("Max waiting update failed after " + DefaultQuality.TIMES + " times");
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
		return StatusTask.ENABLED ? this.waiting.getAndSet(0L) : 0;
	}

	@Override
	public long getIdleAndReset() {
		return StatusTask.ENABLED ? this.idle.getAndSet(0) : 0;
	}
}
