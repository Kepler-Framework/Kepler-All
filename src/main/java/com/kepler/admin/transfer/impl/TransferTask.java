package com.kepler.admin.transfer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.kepler.admin.PeriodTask;
import com.kepler.admin.transfer.Collector;
import com.kepler.admin.transfer.Feeder;
import com.kepler.admin.transfer.Transfers;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim
 *
 * 2016年2月10日
 */
public class TransferTask extends PeriodTask {

	public static final boolean ENABLED = PropertiesUtils.get(TransferTask.class.getName().toLowerCase() + ".enabled", false);

	/**
	 * 默认10秒, 最短5秒, 最长15秒
	 */
	private static final int PERIOD = Math.min(Math.max(5000, PropertiesUtils.get(TransferTask.class.getName().toLowerCase() + ".period", 10000)), 15000);

	private final Collector collector;

	private final Feeder feeder;

	private Collection<Transfers> transfers;

	public TransferTask(Collector collector, Feeder feeder) {
		super();
		this.collector = collector;
		this.feeder = feeder;
	}

	protected PeriodTask prepare() {
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
		Compress compressed = new Compress(this.transfers);
		if (compressed.actived()) {
			this.feeder.feed(compressed.transfers());
		}
	}

	/**
	 * 仅当存在传输数据是标记为激活
	 * 
	 * @author KimShen
	 *
	 */
	private class Compress {

		private final List<Transfers> transfers = new ArrayList<Transfers>();

		private Compress(Collection<Transfers> transfers) {
			for (Transfers each : transfers) {
				CompressTransfers compressed = new CompressTransfers(each.service(), each.version(), each.method(), each.transfers());
				if (compressed.actived()) {
					this.transfers.add(compressed);
				}
			}
		}

		public boolean actived() {
			return !this.transfers.isEmpty();
		}

		public List<Transfers> transfers() {
			return this.transfers;
		}
	}
}
