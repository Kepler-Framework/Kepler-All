package com.kepler.router.filter.sid;

import com.kepler.host.Host;
import com.kepler.host.HostsContext;
import com.kepler.service.Service;

/**
 * 定向过滤
 * 
 * @author kim 2015年11月24日
 */
public class SIDSelector {

	private static final ThreadLocal<Bind> BIND = new ThreadLocal<Bind>() {
		protected Bind initialValue() {
			return new Bind();
		}
	};

	private final HostsContext context;

	public SIDSelector(HostsContext context) {
		super();
		this.context = context;
	}

	public void set(Service service, String sid) {
		// 保存Service-Host
		SIDSelector.BIND.get().reset(service, this.context.getOrCreate(service).select(sid));
	}

	public Host get(Service service) {
		Bind bind = SIDSelector.BIND.get();
		try {
			if (bind.exists() && bind.service(service)) {
				return bind.host();
			}
			return null;
		} finally {
			// 释放
			bind.reset();
		}
	}

	private static class Bind {

		private Service service;

		private Host host;

		private Bind reset() {
			this.service = null;
			this.host = null;
			return this;
		}

		private Bind reset(Service service, Host host) {
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
