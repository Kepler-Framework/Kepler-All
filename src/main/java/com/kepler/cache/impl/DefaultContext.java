package com.kepler.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kepler.cache.Cache;
import com.kepler.cache.CacheContext;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultContext implements Imported, CacheContext {

	/**
	 * 配置格式
	 * 
	 * [method-1,10][method-2,15]
	 */
	private static final String METHOD_KEY = DefaultContext.class.getName().toLowerCase() + ".methods";

	private static final String METHOD_DEF = "";

	private static final Log LOGGER = LogFactory.getLog(DefaultContext.class);

	private final Map<Service, Caches> caches = new HashMap<Service, Caches>();

	/**
	 * 用于没有开启缓存服务的Null Object
	 */
	private final Cache empty = new EmptyCache();

	private final Profile profile;

	public DefaultContext(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		// 预加载Service对应缓存
		this.caches.put(service, new Caches(service, PropertiesUtils.profile(DefaultContext.METHOD_KEY, this.profile.profile(service), DefaultContext.METHOD_DEF)));
	}

	@Override
	public void set(Service service, String method, Object response) {
		// 获取对应Service-method的缓存
		this.caches.get(service).get(method).set(response);
	}

	@Override
	public Cache get(Service service, String method) {
		return this.caches.get(service).get(method);
	}

	/**
	 * 空Cache
	 * 
	 * @author KimShen
	 *
	 */
	private class EmptyCache implements Cache {

		@Override
		public Object get() {
			return null;
		}

		@Override
		public Object set(Object response) {
			return null;
		}

		@Override
		public boolean expired() {
			// 强制超时
			return true;
		}
	}

	/**
	 * @author KimShen
	 *
	 */
	private class DefaultCache implements Cache {

		// 强制首次超时
		private final AtomicLong count = new AtomicLong();

		private final Service service;

		private final String method;

		private final long max;

		private Object response;

		public DefaultCache(Service service, String method, long max) {
			super();
			this.max = max;
			this.method = method;
			this.service = service;
			// 指定为最小值, 强制首次超时
			this.count.set(Long.MIN_VALUE);
		}

		@Override
		public Object get() {
			// 减少已用次数
			this.count.getAndDecrement();
			return this.response;
		}

		@Override
		public Object set(Object response) {
			DefaultContext.LOGGER.info("Reset cache for: " + this.service + " [" + this.method + "] (Warning: ignore override method)");
			// 重置计数
			this.count.set(this.max);
			return this.response;
		}

		@Override
		public boolean expired() {
			// 已用次数小于等于0表示超时
			return this.count.get() <= 0;
		}
	}

	/**
	 * 相对于Servce-method的缓存集合
	 * 
	 * @author KimShen
	 *
	 */
	private class Caches {

		/**
		 * Key=Method, Value=Cache
		 */
		private final Map<String, Cache> caches = new HashMap<String, Cache>();

		/**
		 * 解析配置
		 * 
		 * @param config
		 */
		private Caches(Service service, String config) {
			Matcher matcher = Pattern.compile("(\\[(.*?),(.*?)\\])").matcher(config);
			while (matcher.find()) {
				String method = matcher.group(2);
				String max = matcher.group(3);
				DefaultContext.LOGGER.info("Loading cache: [method=" + method + "][max=" + max + "]");
				// 如果为空则表示为最大值
				this.caches.put(method, new DefaultCache(service, method, StringUtils.isEmpty(max) ? Long.MAX_VALUE : Long.valueOf(max)));
			}
		}

		/**
		 * 获取指定缓存
		 * 
		 * @param method
		 * @return 如果指定方法没有开启缓存则返回EmptyCache
		 */
		public Cache get(String method) {
			Cache cache = this.caches.get(method);
			return cache != null ? cache : DefaultContext.this.empty;
		}
	}
}
