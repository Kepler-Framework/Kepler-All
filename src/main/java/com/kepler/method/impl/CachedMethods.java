package com.kepler.method.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.method.MethodInfo;
import com.kepler.method.Methods;

/**
 * @author KimShen
 *
 */
public class CachedMethods implements Methods {

	private static final Log LOGGER = LogFactory.getLog(CachedMethods.class);

	/**
	 * 缓存方法
	 */
	volatile private Map<CacheKeys, MethodInfo> c_classes = new HashMap<CacheKeys, MethodInfo>();

	volatile private Map<CacheKeys, MethodInfo> c_names = new HashMap<CacheKeys, MethodInfo>();

	volatile private Map<CacheKey, MethodInfo> c_size = new HashMap<CacheKey, MethodInfo>();

	private final Object l_classes = new Object();

	private final Object l_names = new Object();

	private final Object l_size = new Object();

	private final Methods methods;

	private CachedMethods(Methods methods) {
		this.methods = methods;
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, Class<?>[] classes) throws Exception {
		CacheKeys key = new CacheKeys(method, service, classes);
		MethodInfo actual = this.c_classes.get(key);
		if (actual != null) {
			return actual;
		}
		synchronized (this.l_classes) {
			MethodInfo cached = this.c_classes.get(key);
			if (cached != null) {
				return cached;
			}
			MethodInfo refresh_method = this.methods.method(service, method, classes);
			HashMap<CacheKeys, MethodInfo> refresh_cached = new HashMap<CacheKeys, MethodInfo>(this.c_classes);
			refresh_cached.put(key, refresh_method);
			this.c_classes = refresh_cached;
			CachedMethods.LOGGER.warn("Refresh method cache: [service=" + service + "][actual=" + refresh_method + "]");
			return refresh_method;
		}
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, String[] names) throws Exception {
		CacheKeys key = new CacheKeys(method, service, names);
		MethodInfo actual = this.c_names.get(key);
		if (actual != null) {
			return actual;
		}
		synchronized (this.l_names) {
			MethodInfo cached = this.c_names.get(key);
			if (cached != null) {
				return cached;
			}
			MethodInfo refresh_method = this.methods.method(service, method, names);
			HashMap<CacheKeys, MethodInfo> refresh_cached = new HashMap<CacheKeys, MethodInfo>(this.c_names);
			refresh_cached.put(key, refresh_method);
			this.c_names = refresh_cached;
			CachedMethods.LOGGER.warn("Refresh method cache: [service=" + service + "][actual=" + refresh_method + "]");
			return refresh_method;
		}
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, int size) throws Exception {
		CacheKey key = new CacheKey(method, service, size);
		MethodInfo actual = this.c_size.get(key);
		if (actual != null) {
			return actual;
		}
		synchronized (this.l_size) {
			MethodInfo cached = this.c_size.get(key);
			if (cached != null) {
				return cached;
			}
			MethodInfo refresh_method = this.methods.method(service, method, size);
			HashMap<CacheKey, MethodInfo> refresh_cached = new HashMap<CacheKey, MethodInfo>(this.c_size);
			refresh_cached.put(key, refresh_method);
			this.c_size = refresh_cached;
			CachedMethods.LOGGER.warn("Refresh method cache: [service=" + service + "][actual=" + refresh_method + "]");
			return refresh_method;
		}
	}

	/**
	 * 类型及扩展
	 * 
	 * @author KimShen
	 *
	 */
	private class CacheKeys {

		private Object[] addition;

		private Class<?> service;

		private String method;

		private CacheKeys() {
		}

		/**
		 * 常规构造
		 * 
		 * @param method
		 * @param service
		 * @param classes
		 */
		private CacheKeys(String method, Class<?> service, Object[] addition) {
			// 检查是否存在参数
			this.addition = addition;
			this.service = service;
			this.method = method;
		}

		public int hashCode() {
			int hash = 0;
			hash = hash ^ this.service.hashCode() ^ this.method.hashCode();
			if (this.addition != null) {
				for (Object each : this.addition) {
					if (each != null) {
						hash = hash ^ each.hashCode();
					}
				}
			}
			return hash;
		}

		public boolean equals(Object ob) {
			// Guard case1, null
			if (ob == null) {
				return false;
			}
			CacheKeys target = CacheKeys.class.cast(ob);
			// Guard case2, 服务或方法不一致
			if (!this.service.equals(target.service) || !this.method.equals(target.method)) {
				return false;
			}
			if (!Arrays.equals(this.addition, target.addition)) {
				return false;
			}
			return true;
		}
	}

	private class CacheKey {

		private Class<?> service;

		private String method;

		private Object param;

		private CacheKey() {
		}

		/**
		 * 常规构造
		 * 
		 * @param method
		 * @param service
		 * @param classes
		 */
		private CacheKey(String method, Class<?> service, Object param) {
			this.service = service;
			this.method = method;
			this.param = param;
		}

		public int hashCode() {
			int hash = 0;
			hash = hash ^ this.service.hashCode() ^ this.method.hashCode();
			if (this.param != null) {
				hash = hash ^ this.param.hashCode();
			}
			return hash;
		}

		public boolean equals(Object ob) {
			// Guard case1, null
			if (ob == null) {
				return false;
			}
			CacheKey target = CacheKey.class.cast(ob);
			// Guard case2, 服务或方法不一致
			if (!this.service.equals(target.service) || !this.method.equals(target.method)) {
				return false;
			}
			if (this.param == null && target.param != null) {
				return false;
			}
			if (this.param != null && target.param == null) {
				return false;
			}
			if (!this.param.equals(target.param)) {
				return false;
			}
			return true;
		}
	}
}
