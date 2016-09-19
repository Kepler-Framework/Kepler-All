package com.kepler.admin.status.impl;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.config.PropertiesUtils;

/**
 * 启动信息收集
 * 
 * @author kim 2016年1月2日
 */
public class StatusStartup implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	public StatusStartup() {
		this.init();
	}

	private void init() {
		this.init4gc();
		this.init4app();
		this.init4env();
		this.init4memory();
		this.init4system();
		this.init4kepler();
	}

	/**
	 * GC策略收集
	 */
	private void init4gc() {
		List<GarbageCollectorMXBean> collectors = ManagementFactory.getGarbageCollectorMXBeans();
		List<String> names = new ArrayList<String>();
		for (GarbageCollectorMXBean each : collectors) {
			names.add(each.getName());
		}
		this.status.put("gc_names", names);
	}

	/**
	 * 应用信息收集
	 */
	private void init4app() {
		// 启动路径
		this.status.put("app_path", new File("").getAbsolutePath());
		// 启动时间
		this.status.put("app_uptime", ManagementFactory.getRuntimeMXBean().getUptime());
		// 启动参数
		this.status.put("app_arguments", ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
	}

	/**
	 * 环境信息收集
	 */
	private void init4env() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		this.status.put("env_name", runtime.getName());
		this.status.put("env_vm_name", runtime.getVmName());
		this.status.put("env_vm_version", runtime.getVmVersion());
		this.status.put("env_spec_vendor", runtime.getSpecVendor());
		this.status.put("env_spec_version", runtime.getSpecVersion());
	}

	/**
	 * 内存信息收集
	 */
	private void init4memory() {
		MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
		MemoryUsage usage4heap = memory.getHeapMemoryUsage();
		MemoryUsage usage4nonheap = memory.getNonHeapMemoryUsage();
		this.status.put("memory_heap_init", usage4heap.getInit());
		this.status.put("memory_heap_commited", usage4heap.getCommitted());
		this.status.put("memory_nonheap_init", usage4nonheap.getInit());
		this.status.put("memory_nonheap_commited", usage4nonheap.getCommitted());
	}

	/**
	 * 系统信息收集
	 */
	private void init4system() {
		OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
		this.status.put("system_arch", system.getArch());
		this.status.put("system_name", system.getName());
		this.status.put("system_version", system.getVersion());
		this.status.put("system_processors", system.getAvailableProcessors());
	}

	/**
	 * 框架信息收集
	 */
	private void init4kepler() {
		// 常规配置路径
		this.status.put("kepler_path_config", PropertiesUtils.FILE_CONFIG);
		// 动态配置路径
		this.status.put("kepler_path_dynamic", PropertiesUtils.FILE_DYNAMIC);
		// 版本配置路径
		this.status.put("kepler_path_version", PropertiesUtils.FILE_VERSION);
		// 框架版本
		this.status.put("kepler_version", PropertiesUtils.get("kepler.version"));
	}

	@Override
	public Map<String, Object> get() {
		return this.status;
	}
}
