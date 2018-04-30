package com.kepler.generic.reflect.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.impl.DefaultImported;
import com.kepler.generic.reflect.GenericArgs;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.generic.reflect.GenericService;
import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactories;
import com.kepler.serial.Serials;
import com.kepler.serial.generic.GenericSerial;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultService extends DefaultImported implements GenericService {

	private static final boolean GENERIC_SERIAL = PropertiesUtils.get(DefaultService.class.getName().toLowerCase() + ".generic_serial", false);

	/**
	 * 是否自动加载服务
	 */
	private static final boolean AUTOMATIC = PropertiesUtils.get(DefaultService.class.getName().toLowerCase() + ".automatic", true);

	/**
	 * 泛化恒定Class
	 */
	private static final Class<?>[] CLASSES = new Class<?>[] { DelegateArgs.class };

	private final HeadersProcessor processor;

	private final RequestFactories factory;

	private final HeadersContext header;

	private final GenericMarker marker;

	private final IDGenerators gener;

	private final Invoker invoker;

	private final Serials serials;

	public DefaultService(HeadersProcessor processor, RequestFactories factory, IDGenerators gener, HeadersContext header, GenericMarker marker, Imported imported, Serials serials, Invoker invoker) {
		super(imported);
		this.processor = processor;
		this.factory = factory;
		this.serials = serials;
		this.invoker = invoker;
		this.header = header;
		this.marker = marker;
		this.gener = gener;
	}

	private Object invoke(Service service, String method, Object[] args) throws Throwable {
		this.prepare(service);
		byte serial = DefaultService.GENERIC_SERIAL ? GenericSerial.SERIAL : this.serials.def4output().serial();
		// 强制同步调用
		return this.invoker.invoke(this.factory.factory(serial).request(this.headers(service), service, method, false, args, DefaultService.CLASSES, this.gener.get(service, method).generate(), serial), null);
	}

	private DefaultService prepare(Service service) throws Exception {
		if (DefaultService.AUTOMATIC) {
			// 尝试Import服务(如果未注册)
			super.imported(service);
		}
		return this;
	}

	private Headers headers(Service service) throws Throwable {
		// 获取Header并标记为泛型(隐式开启Header)
		return this.marker.mark(this.processor.process(service, this.header.get()));
	}

	public Object invoke(Service service, String method, LinkedHashMap<String, Object> args) throws Throwable {
		return this.invoke(service, method, new DelegateBean(args));
	}

	@Override
	public Object invoke(Service service, String method, String[] classes, Object... args) throws Throwable {
		return this.invoke(service, method, new DelegateArgs(classes, args));
	}

	@Override
	public Object invoke(Service service, String method, GenericArgs args) throws Throwable {
		return this.invoke(service, method, new Object[] { args });
	}

	@Override
	public Object invoke(Service service, String method, GenericBean bean) throws Throwable {
		return this.invoke(service, method, new Object[] { bean });
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericBean invokeAsBean(Service service, String method, LinkedHashMap<String, Object> args) throws Throwable {
		return new DelegateBean(Map.class.cast(this.invoke(service, method, new DelegateBean(args))));
	}

	@Override
	@SuppressWarnings("unchecked")
	public GenericBean invokeAsBean(Service service, String method, String[] classes, Object... args) throws Throwable {
		return new DelegateBean(Map.class.cast(this.invoke(service, method, new DelegateArgs(classes, args))));
	}

	@Override
	@SuppressWarnings("unchecked")
	public GenericBean invokeAsBean(Service service, String method, GenericBean bean) throws Throwable {
		return new DelegateBean(Map.class.cast(this.invoke(service, method, bean)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public GenericBean invokeAsBean(Service service, String method, GenericArgs args) throws Throwable {
		return new DelegateBean(Map.class.cast(this.invoke(service, method, args)));
	}
}
