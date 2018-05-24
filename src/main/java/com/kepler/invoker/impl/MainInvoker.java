package com.kepler.invoker.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.KeplerLocalException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.Request;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月5日
 */
public class MainInvoker implements Imported, Invoker {

	/**
	 * 阀值
	 */
	public static final String THRESHOLD_KEY = MainInvoker.class.getName().toLowerCase() + ".threshold";

	/**
	 * 是否开启流控
	 */
	public static final String THRESHOLD_ENABLED_KEY = MainInvoker.class.getName().toLowerCase() + ".threshold_enabled";

	private static final int THRESHOLD_DEF = PropertiesUtils.get(MainInvoker.THRESHOLD_KEY, Integer.MAX_VALUE);

	private static final boolean THRESHOLD_ENABLED_DEF = PropertiesUtils.get(MainInvoker.THRESHOLD_ENABLED_KEY, false);

	/**
	 * 取代Semaphore, Failed Fast
	 */
	private final Map<Service, AtomicInteger> limit = new HashMap<Service, AtomicInteger>();

	private final List<Invoker> invokers = new ArrayList<Invoker>();

	private final Profile profile;

	public MainInvoker(Profile profile, List<Invoker> invokers) {
		super();
		this.profile = profile;
		this.actived(invokers);
	}

	/**
	 * 加载已激活Invoker
	 * 
	 * @param invokers
	 */
	private void actived(List<Invoker> invokers) {
		for (Invoker each : invokers) {
			if (each.actived()) {
				this.invokers.add(each);
			}
		}
	}

	@Override
	public boolean actived() {
		return true;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		this.limit.put(service, new AtomicInteger());
	}

	public void unsubscribe(Service service) throws Exception {
		this.limit.remove(service);
	}

	@Override
	public Object invoke(Request request, Method method) throws Throwable {
		// 是否开启了流控
		return PropertiesUtils.profile(this.profile.profile(request.service()), MainInvoker.THRESHOLD_ENABLED_KEY, MainInvoker.THRESHOLD_ENABLED_DEF) ? this.check(request, method) : this.uncheck(request, method);
	}

	/**
	 * 流控检查
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object check(Request request, Method method) throws Throwable {
		try {
			// 是否超过阀值
			if (this.limit.get(request.service()).incrementAndGet() < PropertiesUtils.profile(this.profile.profile(request.service()), MainInvoker.THRESHOLD_KEY, MainInvoker.THRESHOLD_DEF)) {
				return this.uncheck(request, method);
			}
			// Failed Fast
			throw new KeplerLocalException("ThresholdInvoker have not enough resources (" + this.limit.get(request.service()).get() + ") ... ");
		} finally {
			// 释放
			this.limit.get(request.service()).decrementAndGet();
		}
	}

	/**
	 * 常规调用
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object uncheck(Request request, Method method) throws Throwable {
		for (Invoker invoker : this.invokers) {
			Object response = invoker.invoke(request, method);
			if (response != Invoker.EMPTY) {
				return response;
			}
		}
		return null;
	}
}
