package com.kepler.management.status.impl;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.management.PeriodTask;
import com.kepler.management.status.Feeder;
import com.kepler.management.status.Status;

/**
 * @author kim
 *
 * 2016年2月10日
 */
public class StatusTask extends PeriodTask {

	/**
	 * 默认60秒, 最小45秒
	 */
	private static final int PERIOD = Math.max(45000, PropertiesUtils.get(StatusTask.class.getName().toLowerCase() + ".period", 60000));

	private static final boolean ENABLED = PropertiesUtils.get(StatusTask.class.getName().toLowerCase() + ".enabled", false);

	private final Status status;

	private final Feeder feeder;

	private final Host local;

	public StatusTask(Status status, Feeder feeder, Host local) {
		super();
		this.status = status;
		this.feeder = feeder;
		this.local = local;
	}

	@Override
	protected long period() {
		return StatusTask.PERIOD;
	}

	@Override
	protected boolean enabled() {
		return StatusTask.ENABLED;
	}

	@Override
	protected void doing() {
		this.feeder.feed(this.local, this.status.get());
	}

}
