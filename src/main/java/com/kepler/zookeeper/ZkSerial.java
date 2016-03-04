package com.kepler.zookeeper;

import com.kepler.host.impl.ServerHost;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.Service;
import com.kepler.service.ServiceInstance;

/**
 * @author kim 2015年7月20日
 */
public class ZkSerial implements ServiceInstance {

	private static final long serialVersionUID = 1L;

	private final ServerHost host;

	private final String version;

	private final String catalog;

	private final String service;

	public ZkSerial(ServerHost host, ServiceInstance instance) {
		super();
		this.host = host;
		this.catalog = instance.catalog();
		this.version = instance.version();
		this.service = instance.service();
	}

	public ZkSerial(ServerHost host, Service service) {
		super();
		this.host = host;
		this.catalog = service.catalog();
		this.version = service.version();
		this.service = service.service().getName();
	}

	public ServerHost host() {
		return this.host;
	}

	public String version() {
		return this.version;
	}

	public String catalog() {
		return this.catalog;
	}

	public String service() {
		return this.service;
	}

	public String versionAndCatalog() {
		return Service.versionAndCatalog(this.version, this.catalog);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}