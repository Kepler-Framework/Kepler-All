package com.kepler.admin.status.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class Status4Runtime implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	public void init() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		this.status.put("runtime_name", runtime.getName());
		this.status.put("runtime_vm_name", runtime.getVmName());
		this.status.put("runtime_vm_version", runtime.getVmVersion());
		this.status.put("runtime_spec_vendor", runtime.getSpecVendor());
		this.status.put("runtime_spec_version", runtime.getSpecVersion());
	}

	@Override
	public Map<String, Object> get() {
		return this.status;
	}
}
