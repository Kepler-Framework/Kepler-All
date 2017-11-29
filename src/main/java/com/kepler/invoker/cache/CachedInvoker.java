package com.kepler.invoker.cache;

import java.lang.reflect.Method;

import com.kepler.cache.Cache;
import com.kepler.cache.CacheContext;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class CachedInvoker implements Invoker {

	/**
	 * 是否开启缓存
	 */
	private static final boolean ACTIVED = PropertiesUtils.get(CachedInvoker.class.getName().toLowerCase() + ".actived", false);

	private final CacheContext cache;

	private final Invoker invoker;

	public CachedInvoker(CacheContext cache, Invoker invoker) {
		super();
		this.cache = cache;
		this.invoker = invoker;
	}

	@Override
	public boolean actived() {
		return CachedInvoker.ACTIVED;
	}

	@Override
	public Object invoke(Request request, Method method) throws Throwable {
		// 开启缓存则尝试使用缓存
		return CachedInvoker.ACTIVED ? this.invoke4cache(request, method) : this.invoker.invoke(request, method);
	}

	/**
	 * 更新缓存
	 * 
	 * @param request
	 * @param cache
	 * @return 本地调用实际结果
	 * @throws Throwable
	 */
	private Object invoke4reset(Request request, Method method, Cache cache) throws Throwable {
		Object response = this.invoker.invoke(request, method);
		cache.set(response);
		return response;
	}

	/**
	 * 使用缓存加载/恢复数据
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object invoke4cache(Request request, Method method) throws Throwable {
		Cache cache = this.cache.get(request.service(), request.method());
		// 如果已过期则更新缓存
		return cache.expired() ? this.invoke4reset(request, method, cache) : cache.get();
	}
}
