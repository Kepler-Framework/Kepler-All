package com.kepler.generic.wrap.impl;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.impl.DefaultImported;
import com.kepler.generic.wrap.GenericArg;
import com.kepler.generic.wrap.GenericService;
import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactories;
import com.kepler.serial.Serials;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultService extends DefaultImported implements GenericService {

	/**
	 * 是否自动加载服务
	 */
	private static final boolean AUTOMATIC = PropertiesUtils.get(DefaultService.class.getName().toLowerCase() + ".automatic", true);

	public DefaultService(HeadersProcessor processor, IDGenerators generators, RequestFactories factory, HeadersContext header, GenericMarker marker, Imported imported, Serials serials, Invoker invoker) {
		super(processor, generators, factory, header, marker, imported, serials, invoker);
	}

	/**
	 * 计算请求Class类型
	 * 
	 * @param args
	 * @return
	 */
	private Class<?>[] clazz(Object... args) {
		Class<?>[] clazz = new Class[args.length];
		for (int index = 0; index < clazz.length; index++) {
			Object arg = args[index];
			// 如果为GenericArg则使用
			clazz[index] = GenericArg.class.isAssignableFrom(arg.getClass()) ? GenericArg.class : arg.getClass();
		}
		return clazz;
	}

	@Override
	public Object invoke(Service service, String method, Object... args) throws Throwable {
		if (DefaultService.AUTOMATIC) {
			// 尝试Import服务(如果未注册)
			this.imported(service);
		}
		// 仅支持默认序列化(兼容性)
		byte serial = super.serials.def4output().serial();
		// 获取Header并标记为泛型(隐式开启Header)
		Headers headers = super.marker.mark(super.processor.process(service, super.header.get()));
		// 强制同步调用
		return this.invoker.invoke(super.factory.factory(serial).request(headers, service, method, false, args, this.clazz(args), super.generators.get(service, method).generate(), serial));
	}
}
