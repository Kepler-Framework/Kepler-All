package com.kepler.admin.status.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class Status4System implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	public void init() {
		OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
		this.status.put("system_arch", system.getArch());
		this.status.put("system_name", system.getName());
		this.status.put("system_version", system.getVersion());
		this.status.put("system_startup", new Date().toString());
		this.status.put("system_processors", system.getAvailableProcessors());
	}

	@Override
	public Map<String, Object> get() {
		return this.status;
	}
}
