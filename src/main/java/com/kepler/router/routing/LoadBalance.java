package com.kepler.router.routing;

import java.util.Iterator;
import java.util.List;

import com.kepler.KeplerRoutingException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.Routing;

/**
 * @author zhangjiehao 2015年9月9日
 */
abstract public class LoadBalance implements Routing {

	/**
	 * 是否开启Location优先级
	 */
	public static final boolean LOCATION_ENABLED = PropertiesUtils.get(LoadBalance.class.getName().toLowerCase() + ".location_enabled", false);

	private static final String LOCATION_PRIORITY_KEY = LoadBalance.class.getName().toLowerCase() + ".location_priority";

	/**
	 * Location优先级默认基数
	 */
	private static final int LOCATION_PRIORITY_DEF = PropertiesUtils.get(LoadBalance.LOCATION_PRIORITY_KEY, 0);

	private final Profile profile;

	public LoadBalance(Profile profile) {
		super();
		this.profile = profile;
	}

	abstract protected int next(int weights);

	/**
	 * 获取指定目标主机对于本次请求的权重
	 * 
	 * @param request
	 * @param host
	 * @return 
	 */
	private int priority(Request request, Host host) {
		// 计算基数, 如果未开启则使用默认基数
		int multi = LoadBalance.LOCATION_ENABLED ? PropertiesUtils.profile(this.profile.profile(request.service()), LoadBalance.LOCATION_PRIORITY_KEY + "." + host.location(), LoadBalance.LOCATION_PRIORITY_DEF) : LoadBalance.LOCATION_PRIORITY_DEF;
		return multi + host.priority();
	}

	@Override
	public Host route(Request request, List<Host> hosts) {
		// 如果仅一台主机则立即返回
		if (hosts.size() == 1) {
			return hosts.get(0);
		}
		Slots slots = new Slots(request, hosts);
		int cursor = this.next(this.sumUpWeight(request, hosts));
		while (slots.hasNext()) {
			if (cursor < slots.next()) {
				return slots.host();
			}
		}
		throw new KeplerRoutingException("None right service for " + request.service());
	}

	/**
	 * 计算全部权重
	 * 
	 * @param request
	 * @param hosts
	 * @return
	 */
	private int sumUpWeight(Request request, List<Host> hosts) {
		int totalWeight = 0;
		if (!hosts.isEmpty()) {
			for (Host host : hosts) {
				totalWeight += this.priority(request, host);
			}
		}
		return totalWeight;
	}

	private class Slots implements Iterator<Integer> {

		private final List<Host> hosts;

		private final Request request;

		private int currentHost = -1;

		private int currentSlot = 0;

		private Slots(Request request, List<Host> hosts) {
			this.request = request;
			this.hosts = hosts;
		}

		@Override
		public boolean hasNext() {
			return this.currentHost < this.hosts.size() - 1;
		}

		@Override
		public Integer next() {
			this.currentHost += 1;
			this.currentSlot += LoadBalance.this.priority(this.request, this.hosts.get(this.currentHost));
			return this.currentSlot;
		}

		public Host host() {
			return this.hosts.get(this.currentHost);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
