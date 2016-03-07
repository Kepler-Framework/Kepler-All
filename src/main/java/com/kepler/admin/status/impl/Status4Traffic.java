package com.kepler.admin.status.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.traffic.Traffic;

/**
 * @author kim 2016年1月7日
 */
public class Status4Traffic implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	private final Traffic traffic;

	public Status4Traffic(Traffic traffic) {
		super();
		this.traffic = traffic;
	}

	@Override
	public Map<String, Object> get() {
		this.status.put("traffic_input", this.traffic.getInputAndReset());
		this.status.put("traffic_output", this.traffic.getOutputAndReset());
		return this.status;
	}
}
