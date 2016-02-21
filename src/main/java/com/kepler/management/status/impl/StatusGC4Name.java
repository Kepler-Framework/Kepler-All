package com.kepler.management.status.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kepler.management.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class StatusGC4Name implements Status {

	private final List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

	private final Map<String, Object> names = new HashMap<String, Object>();

	public void init() {
		// GC策略
		List<String> gc = new ArrayList<String>();
		for (GarbageCollectorMXBean each : this.gcs) {
			gc.add(each.getName());
		}
		this.names.put("gc_names", gc);
	}

	@Override
	public Map<String, Object> get() {
		return this.names;
	}
}
