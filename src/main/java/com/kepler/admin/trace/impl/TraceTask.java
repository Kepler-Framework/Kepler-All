package com.kepler.admin.trace.impl;

import java.util.List;

import com.kepler.admin.PeriodTask;
import com.kepler.admin.status.impl.StatusTask;
import com.kepler.admin.trace.Feeder;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCauses;

/**
 * @author kim
 *
 */
public class TraceTask extends PeriodTask {

	/**
	 * 默认Status监控开启 Trace监控开启
	 */
	public static final boolean ENABLED = PropertiesUtils.get(TraceTask.class.getName().toLowerCase() + ".enabled", StatusTask.ENABLED);

	/**
	 * 默认45秒, 最小30秒
	 */
	private static final int PERIOD = Math.max(30000, PropertiesUtils.get(TraceTask.class.getName().toLowerCase() + ".period", 45000));

	private final TraceCauses trace;

	private final Feeder feeder;

	private final Host host;

	public TraceTask(TraceCauses trace, Feeder feeder, Host host) {
		super();
		this.host = host;
		this.trace = trace;
		this.feeder = feeder;
	}

	@Override
	protected long period() {
		return TraceTask.PERIOD;
	}

	@Override
	protected boolean enabled() {
		return TraceTask.ENABLED;
	}

	@Override
	protected void doing() {
		List<TraceCause> causes = this.trace.get();
		if (!causes.isEmpty()) {
			this.feeder.feed(this.host, causes);
		}
	}
}
