package com.kepler.management.status.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kepler.management.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class StatusGC4Data implements Status {

	private final List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

	private final Map<String, Long> snapshot = new HashMap<String, Long>();

	public void init() {
		// GC名称及其初始值
		for (GarbageCollectorMXBean each : this.gcs) {
			this.snapshot.put("gc_" + each.getName().toLowerCase() + "_time", 0L);
			this.snapshot.put("gc_" + each.getName().toLowerCase() + "_count", 0L);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> get() {
		for (GarbageCollectorMXBean each : this.gcs) {
			// 更新GC
			String key4time = "gc_" + each.getName().toLowerCase() + "_time";
			String key4count = "gc_" + each.getName().toLowerCase() + "_count";
			// 取差值
			this.snapshot.put(key4time, each.getCollectionTime() - this.snapshot.get(key4time));
			this.snapshot.put(key4count, each.getCollectionCount() - this.snapshot.get(key4count));
		}
		return (Map<String, Object>) (Map<String, ?>) this.snapshot;
	}
}
