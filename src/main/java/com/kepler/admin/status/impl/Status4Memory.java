package com.kepler.admin.status.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class Status4Memory implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

	@Override
	public Map<String, Object> get() {
		MemoryUsage usage4heap = this.memory.getHeapMemoryUsage();
		MemoryUsage usage4nonheap = this.memory.getNonHeapMemoryUsage();
		this.status.put("memory_heap_max", usage4heap.getMax());
		this.status.put("memory_heap_used", usage4heap.getUsed());
		this.status.put("memory_heap_init", usage4heap.getInit());
		this.status.put("memory_heap_commited", usage4heap.getCommitted());
		this.status.put("memory_nonheap_max", usage4nonheap.getMax());
		this.status.put("memory_nonheap_used", usage4nonheap.getUsed());
		this.status.put("memory_nonheap_init", usage4nonheap.getInit());
		this.status.put("memory_nonheap_commited", usage4nonheap.getCommitted());
		return this.status;
	}
}
