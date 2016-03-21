package com.kepler.admin.status.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.connection.Counter;

/**
 * @author kim
 *
 * 2016年3月18日
 */
public class Status4Request implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	private final Counter counter;

	public Status4Request(Counter counter) {
		super();
		this.counter = counter;
	}

	@Override
	public Map<String, Object> get() {
		this.status.put("request", this.counter.remain());
		return this.status;
	}
}
