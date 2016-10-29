package com.kepler.generic.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.generic.GenericImported;
import com.kepler.generic.GenericMarker;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactory;
import com.kepler.serial.Serials;
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

	protected final HeadersProcessor processor;

	protected final IDGenerators generators;

	protected final RequestFactory factory;

	protected final HeadersContext header;

	protected final GenericMarker marker;

	protected final Imported imported;

	protected final Serials serials;

	protected final Invoker invoker;

	/**
	 * 已注册服务
	 */
	volatile private Set<Service> services;

	public DefaultImported(HeadersProcessor processor, IDGenerators generators, RequestFactory factory, HeadersContext header, GenericMarker marker, Imported imported, Serials serials, Invoker invoker) {
		super();
		this.services = new HashSet<Service>();
		this.generators = generators;
		this.processor = processor;
		this.imported = imported;
		this.factory = factory;
		this.serials = serials;
		this.invoker = invoker;
		this.header = header;
		this.marker = marker;
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
				Set<Service> services = new HashSet<Service>();
				for (Service each : this.services) {
					services.add(each);
				}
				this.services = services;
				this.services.add(service);
				this.imported.subscribe(service);
				DefaultImported.LOGGER.warn("Import generic service: " + service + ", and will not be uninstalled until server closed");
			}
		}
	}
}
