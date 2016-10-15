package com.kepler.admin;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kim
 *
 * 2016年2月10日
 */
abstract public class PeriodTask implements Delayed {

	private static final Log LOGGER = LogFactory.getLog(PeriodTask.class);

	protected long deadline;

	/**
	 * 准备下一次周期时间
	 */
	protected PeriodTask prepare() {
		this.deadline = System.currentTimeMillis() + this.period();
		PeriodTask.LOGGER.debug("Period prepare dealine to " + new Date(this.deadline) + " on " + this.toString() + " ... ");
		return this;
	}

	public int compareTo(Delayed o) {
		return this.getDelay(TimeUnit.SECONDS) >= o.getDelay(TimeUnit.SECONDS) ? 1 : -1;
	}

	public long getDelay(TimeUnit unit) {
		return unit.convert(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void command() {
		if (this.enabled()) {
			this.doing();
		}
	}

	/**
	 * 实际执行
	 * 
	 */
	abstract protected void doing();

	/**
	 * 周期时间
	 * 
	 * @return
	 */
	abstract protected long period();

	abstract protected boolean enabled();
}
