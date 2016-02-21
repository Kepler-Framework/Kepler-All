package com.kepler.management.dependency.impl;

import java.util.Set;

import com.kepler.host.Host;
import com.kepler.management.dependency.Dependency;
import com.kepler.management.dependency.DependencyService;

/**
 * Host + 一组Service
 * 
 * @author kim 2015年12月30日
 */
public class ImportedServices implements Dependency {

	private final static long serialVersionUID = 1L;

	private final Set<DependencyService> services;

	private final Host host;

	public ImportedServices(Host host, Set<DependencyService> services) {
		super();
		this.host = host;
		this.services = services;
	}

	@Override
	public Set<DependencyService> services() {
		return this.services;
	}

	@Override
	public Host host() {
		return this.host;
	}
}
