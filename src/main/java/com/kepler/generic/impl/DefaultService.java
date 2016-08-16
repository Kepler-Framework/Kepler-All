package com.kepler.generic.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.GenericService;
import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactory;
import com.kepler.serial.Serials;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultService implements GenericService {

	/**
	 * 是否自动加载服务
	 */
	private static final boolean AUTOMATIC = PropertiesUtils.get(DefaultService.class.getName().toLowerCase() + ".automatic", true);

	private static final Log LOGGER = LogFactory.getLog(DefaultService.class);

	/**
	 * 泛化恒定Class
	 */
	private final Class<?>[] classes = new Class<?>[] { DelegateArgs.class };

	/**
	 * 已注册服务
	 */
	private final Set<Service> services = new HashSet<Service>();

	private final HeadersProcessor processor;

	private final IDGenerators generators;

	private final RequestFactory factory;

	private final HeadersContext header;

	private final GenericMarker marker;

	private final Imported imported;

	private final Serials serials;

	private final Invoker invoker;

	public DefaultService(HeadersProcessor processor, IDGenerators generators, RequestFactory factory, HeadersContext header, GenericMarker marker, Imported imported, Serials serials, Invoker invoker) {
		super();
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
			this.imported.subscribe(service);
			this.services.add(service);
			DefaultService.LOGGER.warn("Import generic service: " + service + ", and will not be uninstalled until server closed");
		}
	}

	@Override
	public Object invoke(Service service, String method, String[] classes, Object... args) throws Throwable {
		if (DefaultService.AUTOMATIC) {
			// 尝试Import服务(如果未注册)
			this.imported(service);
		}
		// 仅支持默认序列化(兼容性)
		byte serial = this.serials.def4output().serial();
		// 获取Header并标记为泛型(隐式开启Header)
		Headers headers = this.marker.mark(this.processor.process(service, this.header.get()));
		// 强制同步调用
		return this.invoker.invoke(this.factory.request(headers, service, method, false, new Object[] { new DelegateArgs(classes, args) }, this.classes, this.generators.get(service, method).generate(), serial));
	}
}
