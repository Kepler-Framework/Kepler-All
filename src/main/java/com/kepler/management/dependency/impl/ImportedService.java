package com.kepler.management.dependency.impl;

import com.kepler.management.dependency.DependencyService;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.builder.HashCodeBuilder;
import com.kepler.service.Service;

/**
 * @author kim 2015年12月30日
 */
public class ImportedService implements DependencyService {

	private final static long serialVersionUID = 1L;

	private final String service;

	private final String version;

	public ImportedService(Service service) {
		this.service = service.service().getName();
		this.version = service.versionAndCatalog();
	}

	public ImportedService(String service, String version) {
		this.service = service;
		this.version = version;
	}

	@Override
	public String service() {
		return this.service;
	}

	@Override
	public String versionAndCatalog() {
		return this.version;
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object ob) {
		DependencyService service = DependencyService.class.cast(ob);
		return StringUtils.equals(this.service(), service.service()) && StringUtils.equals(this.versionAndCatalog(), service.versionAndCatalog());
	}
}
