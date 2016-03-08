package com.kepler.service.imported;

import java.io.Serializable;

import com.kepler.host.Host;
import com.kepler.service.Service;

/**
 * 服务依赖 
 *
 * @author kim
 *
 * 2016年3月4日
 */
public class ImportedService implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String sid;

	private final String name;

	private final String group;

	private final String service;

	private final String versionAndCatalog;

	public ImportedService(Host host, Service service) {
		this.sid = host.sid();
		this.name = host.name();
		this.group = host.group();
		this.service = service.service().getName();
		this.versionAndCatalog = service.versionAndCatalog();
	}

	public String sid() {
		return this.sid;
	}

	public String name() {
		return this.name;
	}

	public String group() {
		return this.group;
	}

	public String service() {
		return this.service;
	}

	public String versionAndCatalog() {
		return this.versionAndCatalog;
	}
}
