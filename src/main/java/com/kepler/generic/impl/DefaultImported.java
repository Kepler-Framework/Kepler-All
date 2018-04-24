package com.kepler.generic.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.generic.GenericImported;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * 服务导入
 * 
 * @author KimShen
 *
 */
abstract public class DefaultImported implements GenericImported {

	private static final Log LOGGER = LogFactory.getLog(DefaultImported.class);

	protected final Imported imported;

	/**
	 * 已注册服务
	 */
	volatile private Set<Service> services;

	public DefaultImported(Imported imported) {
		super();
		this.services = new HashSet<Service>();
		this.imported = imported;
	}

	public void imported(Service service) throws Exception {
		// 仅加载尚未加载的服务
		if (!this.services.contains(service)) {
			synchronized (this) {
				// Double check
				if (this.services.contains(service)) {
					return;
				}
				// Copy Write
				Set<Service> services = new HashSet<Service>(this.services);
				services.add(service);
				this.services = services;
				this.imported.subscribe(service);
				DefaultImported.LOGGER.warn("Import generic service: " + service);
			}
		}
	}
}
