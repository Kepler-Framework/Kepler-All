package com.kepler.generic.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericArg;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.GenericService;
import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactory;
import com.kepler.serial.SerialID;
import com.kepler.serial.Serials;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultService implements GenericService {

	private static final Log LOGGER = LogFactory.getLog(DefaultService.class);

	private final Set<Service> services = new HashSet<Service>();

	private final HeadersProcessor processor;

	private final IDGenerators generators;

	private final RequestFactory factory;

	private final HeadersContext header;

	private final GenericMarker marker;

	private final Imported imported;

	private final Profile profile;

	private final Serials serials;

	private final Invoker invoker;

	public DefaultService(HeadersProcessor processor, IDGenerators generators, RequestFactory factory, HeadersContext header, GenericMarker marker, Imported imported, Profile profile, Serials serials, Invoker invoker) {
		super();
		this.generators = generators;
		this.processor = processor;
		this.imported = imported;
		this.factory = factory;
		this.profile = profile;
		this.serials = serials;
		this.invoker = invoker;
		this.header = header;
		this.marker = marker;
	}

	/**
	 * 手动加载服务
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void imported(Service service) throws Exception {
		// 仅加载尚未加载的服务
		if (!this.services.contains(service)) {
			this.imported.subscribe(service);
			this.services.add(service);
			DefaultService.LOGGER.warn("Import generic service: " + service + ", and will not be uninstalled until client closed");
		}
	}

	/**
	 * 参数占位符, 创建等同于参数数量的Class
	 * 
	 * @param args
	 * @return
	 */
	private Class<?>[] clazz(GenericArg... args) {
		Class<?>[] clazz = new Class[args.length];
		for (int index = 0; index < clazz.length; index++) {
			clazz[index] = GenericArg.class;
		}
		return clazz;
	}

	@Override
	public Object invoke(Service service, String method, GenericArg... args) throws Throwable {
		// 尝试Import服务(如果未注册)
		this.imported(service);
		// PropertiesUtils.profile(ImportedServiceFactory.this.profile.profile(service), SerialID.Serial.SERIAL_KEY, SerialID.Serial.SERIAL_VAL)), 获取与Service相关的序列化策略, 并将String转换为对应Byte
		byte serial = SerialID.DYAMIC ? this.serials.output(PropertiesUtils.profile(this.profile.profile(service), SerialID.SERIAL_KEY, SerialID.SERIAL_VAL)) : this.serials.output(SerialID.SERIAL_VAL);
		// 获取Header并标记为泛型
		Headers headers = this.marker.mark(this.processor.process(service, this.header.get()));
		// 强制同步调用
		return this.invoker.invoke(this.factory.request(headers, service, method, false, args, this.clazz(args), this.generators.get(service, method).generate(), serial));
	}
}
