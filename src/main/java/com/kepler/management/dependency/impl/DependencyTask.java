package com.kepler.management.dependency.impl;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.host.HostsContext;
import com.kepler.management.PeriodTask;
import com.kepler.management.dependency.Feeder;

/**
 * @author kim
 *
 * 2016年2月10日
 */
public class DependencyTask extends PeriodTask {

	/**
	 * 默认60秒, 最小30秒
	 */
	private final static int PERIOD = Math.max(30000, PropertiesUtils.get(DependencyTask.class.getName().toLowerCase() + ".period", 60000));

	/**
	 * 默认600秒, 最小300秒
	 */
	private final static int FORCE = Math.max(300000, PropertiesUtils.get(DependencyTask.class.getName().toLowerCase() + ".force", 600000));

	private final static boolean ENABLED = PropertiesUtils.get(DependencyTask.class.getName().toLowerCase() + ".enabled", false);

	private final HostsContext context;

	private final Feeder feeder;

	private final Host host;

	private long previous;

	/**
	 * 当前依赖的Hash值
	 */
	private int hashed;

	public DependencyTask(HostsContext context, Feeder feeder, Host host) {
		super();
		this.context = context;
		this.feeder = feeder;
		this.host = host;
	}

	/**
	 * 是否需要提交
	 * 
	 * @param dependency
	 * @return
	 */
	private boolean trigger(ImportedDependencies dependency) {
		// Hash值变化或需要强制提交
		return this.hashed != dependency.hash() || (this.previous + DependencyTask.FORCE) < System.currentTimeMillis();
	}

	/**
	 * 修改状态
	 * 
	 * @param dependency
	 * @return
	 */
	private DependencyTask change(ImportedDependencies dependency) {
		this.previous = System.currentTimeMillis();
		// 本次提交Hash值
		this.hashed = dependency.hash();
		return this;
	}

	@Override
	protected long period() {
		return DependencyTask.PERIOD;
	}

	protected boolean enabled() {
		return DependencyTask.ENABLED;
	}

	@Override
	protected void doing() {
		ImportedDependencies dependency = new ImportedDependencies(this.context.hosts());
		if (this.trigger(dependency)) {
			this.change(dependency).feeder.feed(this.host, dependency.dependency());
		}
	}
}
