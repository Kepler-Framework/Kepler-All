package com.kepler.management.status.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import com.kepler.management.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class Status4Running implements Status {

	private final OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();

	private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
	
	private final Map<String, Object> status = new HashMap<String, Object>();

	@Override
	public Map<String, Object> get() {
		this.status.put("running_uptime", this.runtime.getUptime());
		this.status.put("running_loadaverage", this.system.getSystemLoadAverage());
		return this.status;
	}
}
