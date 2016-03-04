package com.kepler.management.transfer.impl;

import java.util.Collection;

import com.kepler.config.PropertiesUtils;
import com.kepler.management.PeriodTask;
import com.kepler.management.transfer.Collector;
import com.kepler.management.transfer.Feeder;
import com.kepler.management.transfer.Transfers;

/**
 * @author kim
 *
 * 2016年2月10日
 */
public class TransferTask extends PeriodTask {

	/**
	 * 默认30秒, 最短15秒, 最长45秒
	 */
	private static final int PERIOD = Math.min(Math.max(15000, PropertiesUtils.get(TransferTask.class.getName().toLowerCase() + ".period", 30000)), 45000);

	private static final boolean ENABLED = PropertiesUtils.get(TransferTask.class.getName().toLowerCase() + ".enabled", false);

	private final Collector collector;

	private final Feeder feeder;

	volatile private Collection<Transfers> transfers;

	public TransferTask(Collector collector, Feeder feeder) {
		super();
		this.collector = collector;
		this.feeder = feeder;
	}

	protected PeriodTask prepare() {
		// 切换Collector(For AckTimeout)
		this.transfers = this.collector.transfers();
		return super.prepare();
	}

	@Override
	protected long period() {
		return TransferTask.PERIOD;
	}

	@Override
	protected boolean enabled() {
		return TransferTask.ENABLED;
	}

	@Override
	protected void doing() {
		this.feeder.feed(System.currentTimeMillis(), this.transfers);
	}
}
