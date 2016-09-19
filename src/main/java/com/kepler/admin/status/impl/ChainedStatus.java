package com.kepler.admin.status.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.kepler.admin.status.Status;

/**
 * @author kim 2015年8月10日
 */
public class ChainedStatus implements Status {

	private final Map<String, Object> result = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

	private final List<Status> status;

	public ChainedStatus(List<Status> status) {
		super();
		this.status = status;
	}

	@Override
	public Map<String, Object> get() {
		//重置
		this.result.clear();
		for (Status each : this.status) {
			this.result.putAll(each.get());
		}
		return this.result;
	}
}
