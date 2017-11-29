package com.kepler.zookeeper;

import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.ServiceInstance;

/**
 * @author KimShen
 *
 */
public class ZkInstance {

	private final ServiceInstance instance;

	private final String path;

	public ZkInstance(ServiceInstance instance, String path) {
		super();
		this.instance = instance;
		this.path = path;
	}

	public ServiceInstance instance() {
		return this.instance;
	}

	public String path() {
		return this.path;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
