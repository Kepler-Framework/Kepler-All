package com.kepler.router.filter.impl;

import com.kepler.host.Host;
import com.kepler.host.HostsContext;
import com.kepler.service.Service;

/**
 * 定向过滤
 * 
 * @author kim 2015年11月24日
 */
public class AssumedSelector {

	private static final ThreadLocal<Assumed> ASSUMED = new ThreadLocal<Assumed>() {
		protected Assumed initialValue() {
			return new Assumed();
		}
	};

	private final HostsContext context;

	public AssumedSelector(HostsContext context) {
		super();
		this.context = context;
	}

	public void set(Service service, String address) {
		// 保存Service-Host
		AssumedSelector.ASSUMED.get().reset(service, this.context.getOrCreate(service).select(address));
	}

	public Host release(Service service) {
		Assumed assumed = AssumedSelector.ASSUMED.get();
		try {
			// Service相同
			if (assumed.exists() && assumed.service(service)) {
				return assumed.host();
			}
			return null;
		} finally {
			// 释放
			assumed.reset();
		}
	}

	private static class Assumed {

		private Service service;

		private Host host;

		private Assumed reset() {
			this.service = null;
			this.host = null;
			return this;
		}

		private Assumed reset(Service service, Host host) {
			this.service = service;
			this.host = host;
			return this;
		}

		public Host host() {
			return this.host;
		}

		public boolean exists() {
			return this.host() != null;
		}

		public boolean service(Service service) {
			return this.service.equals(service);
		}
	}
}
