package com.kepler.admin.status.impl;

import java.util.List;

import com.kepler.admin.PeriodTask;
import com.kepler.admin.status.Refresh;
import com.kepler.config.PropertiesUtils;

/**
 * @author KimShen
 *
 */
public class RefreshTask extends PeriodTask {

	/**
	 * 默认10秒, 最大15秒
	 */
	private static final int PERIOD = Math.min(15000, PropertiesUtils.get(RefreshTask.class.getName().toLowerCase() + ".period", 10000));

	private final List<Refresh> refresh;

	public RefreshTask(List<Refresh> refresh) {
		super();
		this.refresh = refresh;
	}

	@Override
	protected long period() {
		return RefreshTask.PERIOD;
	}

	@Override
	protected boolean enabled() {
		return StatusTask.ENABLED;
	}

	@Override
	protected void doing() {
		for (Refresh each : this.refresh) {
			each.refresh();
		}
	}
}
