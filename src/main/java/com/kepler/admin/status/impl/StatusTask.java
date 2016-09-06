package com.kepler.admin.status.impl;

import com.kepler.admin.PeriodTask;
import com.kepler.admin.status.Feeder;
import com.kepler.admin.status.Status;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;

/**
 * @author kim
 *
 * 2016年2月10日
 */
public class StatusTask extends PeriodTask {

	public static final boolean ENABLED = PropertiesUtils.get(StatusTask.class.getName().toLowerCase() + ".enabled", false);

	/**
	 * 默认45秒, 最小30秒
	 */
	private static final int PERIOD = Math.max(30000, PropertiesUtils.get(StatusTask.class.getName().toLowerCase() + ".period", 45000));

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
