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

	/**
	 * @param host 依赖服务的实例
	 * @param service 被依赖服务
	 */
	public ImportedService(Host host, Service service) {
		this.sid = host.sid();
		this.name = host.name();
		this.group = host.group();
		this.service = service.service();
		this.versionAndCatalog = service.versionAndCatalog();
	}

	/**
	 * 依赖服务的实例SID
	 * 
	 * @return
	 */
	public String sid() {
		return this.sid;
	}

	/**
	 * 依赖服务的实例名称
	 * 
	 * @return
	 */
	public String name() {
		return this.name;
	}

	/**
	 * 依赖服务的实例分组
	 * 
	 * @return
	 */
	public String group() {
		return this.group;
	}

	/**
	 * 被依赖的服务
	 * 
	 * @return
	 */
	public String service() {
		return this.service;
	}

	/**
	 * 被依赖的服务版本
	 * 
	 * @return
	 */
	public String versionAndCatalog() {
		return this.versionAndCatalog;
	}
}
