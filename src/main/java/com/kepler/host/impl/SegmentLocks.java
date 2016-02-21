package com.kepler.host.impl;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.host.HostLocks;

/**
 * @author kim 2015年7月14日
 */
public class SegmentLocks implements HostLocks {

	private final static int LOCKS = PropertiesUtils.get(SegmentLocks.class.getName().toLowerCase() + ".locks", 10);

	private final Object[] locks = new Object[SegmentLocks.LOCKS];

	public SegmentLocks() {
		for (int index = 0; index < this.locks.length; index++) {
			this.locks[index] = new Object();
		}
	}

	public Object get(Host host) {
		// 不使用Host.hashcode, 因为包含PID Hash
		return this.locks[Math.abs(host.host().hashCode() ^ host.port()) % this.locks.length];
	}
}