package com.kepler.admin.status.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import com.kepler.admin.status.Refresh;
import com.kepler.config.PropertiesUtils;

/**
 * 动态收集
 * 
 * @author kim 2016年1月2日
 */
public class StatusMemory extends StatusDynamic implements Refresh {

	// 允许收集的最大数量(每个周期)
	private static final byte MAX = PropertiesUtils.get(StatusMemory.class.getName().toLowerCase() + ".max", (byte) 10);

	private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

	public StatusMemory() {
		super(new String[] { "memory_heap_max", "memory_heap_used" });
	}

	@Override
	public void refresh() {
		MemoryUsage usage4heap = this.memory.getHeapMemoryUsage();
		// 当前时间
		long current = System.currentTimeMillis();
		super.add("memory_heap_max", current, usage4heap.getMax());
		super.add("memory_heap_used", current, usage4heap.getUsed());
	}

	@Override
	protected byte max() {
		return StatusMemory.MAX;
	}
}
