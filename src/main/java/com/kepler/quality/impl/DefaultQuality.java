package com.kepler.quality.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.admin.status.impl.StatusTask;
import com.kepler.quality.Quality;

/**
 * @author KimShen
 *
 */
public class DefaultQuality implements Quality {

	private final AtomicLong breaking = new AtomicLong();

	private final AtomicLong demoting = new AtomicLong();

	private final AtomicLong waiting = new AtomicLong();

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
			this.waiting.incrementAndGet();
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
		return StatusTask.ENABLED ? this.waiting.getAndSet(0) : 0;
	}
}
