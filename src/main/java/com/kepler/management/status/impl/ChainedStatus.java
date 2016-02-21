package com.kepler.management.status.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.kepler.management.status.Status;

/**
 * @author kim 2015年8月10日
 */
public class ChainedStatus implements Status {

	private final List<Status> status;

	public ChainedStatus(List<Status> status) {
		super();
		this.status = status;
	}

	@Override
	public Map<String, Object> get() {
		Map<String, Object> status = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		for (Status each : this.status) {
			status.putAll(each.get());
		}
		return status;
	}
}
