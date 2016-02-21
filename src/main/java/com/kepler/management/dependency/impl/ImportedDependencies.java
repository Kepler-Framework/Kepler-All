package com.kepler.management.dependency.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kepler.host.Host;
import com.kepler.host.HostState;
import com.kepler.host.Hosts;
import com.kepler.management.dependency.Dependency;
import com.kepler.management.dependency.DependencyService;
import com.kepler.service.Service;

/**
 * Host -> Services反向重组
 * 
 * @author kim 2015年12月30日
 */
public class ImportedDependencies extends HashMap<Host, Set<DependencyService>> {

	private final static long serialVersionUID = 1L;

	private int hash = 0;

	public ImportedDependencies(Map<Service, Hosts> dependency) {
		for (Service each : dependency.keySet()) {
			// 对已激活主机-Service重组合
			for (Host host : dependency.get(each).select(HostState.ACTIVE)) {
				this.group(host, each);
			}
		}
	}

	private void group(Host host, Service each) {
		DependencyService service = new ImportedService(each);
		// 是否已存在对应分组, 不存在则创建
		Set<DependencyService> services = super.get(host);
		(services = services != null ? services : new HashSet<DependencyService>()).add(service);
		// 更新主机,及其依赖服务列表
		super.put(host, services);
		// 更新Hash
		this.hash = this.hash ^ service.hashCode();
	}

	public Set<Dependency> dependency() {
		Set<Dependency> dependency = new HashSet<Dependency>();
		for (Host host : super.keySet()) {
			dependency.add(new ImportedServices(host, super.get(host)));
		}
		return dependency;
	}

	public int hash() {
		return this.hash;
	}
}
