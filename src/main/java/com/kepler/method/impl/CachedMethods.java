package com.kepler.method.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.method.Methods;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class CachedMethods implements Methods {

	/**
	 * 是否开启方法缓存, 如果关闭则每次均使用调用
	 */
	private static final boolean ENABLED = PropertiesUtils.get(CachedMethods.class.getName().toLowerCase() + ".enabled", true);

	private static final Log LOGGER = LogFactory.getLog(CachedMethods.class);

	/**
	 * 缓存方法
	 */
	volatile private Map<ServiceAndMethod, Method> cached = new HashMap<ServiceAndMethod, Method>();

	/**
	 * 获取并缓存方法
	 * 
	 * @param service_method
	 * @return
	 * @throws Exception
	 */
	private Method cached(ServiceAndMethod service_method) throws Exception {
		// 实际方法
		Method actual = MethodUtils.getMatchingAccessibleMethod(service_method.service(), service_method.method(), service_method.classes());
		if (actual != null) {
			synchronized (this) {
				// 同步检查
				if (this.cached.containsKey(service_method)) {
					return actual;
				}
				CachedMethods.LOGGER.warn("Refresh method cache: [service=" + service_method + "][actual=" + actual + "]");
				HashMap<ServiceAndMethod, Method> cached = new HashMap<ServiceAndMethod, Method>(this.cached);
				cached.put(service_method.clone(), actual);
				this.cached = cached;
			}
		}
		return actual;
	}

	@Override
	public Method method(Class<?> service, String method, Class<?>[] parameter) throws Exception {
		ServiceAndMethod service_method = new ServiceAndMethod(method, service, parameter);
		if (CachedMethods.ENABLED) {
			Method matched = Method.class.cast(this.cached.get(service_method));
			// 如果缓存已存在则返回, 否则查找方法并加入缓存
			return matched != null ? matched : this.cached(service_method);
		} else {
			return MethodUtils.getAccessibleMethod(service_method.service(), service_method.method(), service_method.classes());
		}
	}

	/**
	 * 类型及扩展
	 * 
	 * @author KimShen
	 *
	 */
	private static class ServiceAndMethod implements Cloneable {

		private static final Class<?>[] EMPTY = new Class<?>[0];

		private String method;

		private Class<?> service;

		private Class<?>[] classes;

		private ServiceAndMethod() {
		}

		/**
		 * 常规构造
		 * 
		 * @param method
		 * @param service
		 * @param classes
		 */
		private ServiceAndMethod(String method, Class<?> service, Class<?>[] classes) {
			this.method = method;
			this.service = service;
			// 检查是否存在参数
			this.classes = classes != null ? classes : ServiceAndMethod.EMPTY;
		}

		public String method() {
			return this.method;
		}

		public Class<?> service() {
			return this.service;
		}

		public Class<?>[] classes() {
			return this.classes;
		}

		public int hashCode() {
			int hash = 0;
			hash = hash ^ this.service.hashCode() ^ this.method.hashCode();
			for (Class<?> each : this.classes) {
				hash = hash ^ each.hashCode();
			}
			return hash;
		}

		public boolean equals(Object ob) {
			// Guard case1, null
			if (ob == null) {
				return false;
			}
			ServiceAndMethod target = ServiceAndMethod.class.cast(ob);
			// Guard case2, 服务或方法不一致
			if (!this.service.equals(target.service) || !this.method.equals(target.method)) {
				return false;
			}
			// Guard case3, 参数长度不相等
			if (this.classes.length != target.classes.length) {
				return false;
			}
			// Guard case3, 参数类型不相等
			for (int index = 0; index < this.classes.length; index++) {
				if (!this.classes[index].equals(target.classes[index])) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 深复制
		 * 
		 * @return
		 */
		public ServiceAndMethod clone() {
			return new ServiceAndMethod(this.method, this.service, this.classes);
		}

		public String toString() {
			return "[service=" + this.service + "][method=" + this.method + "[classes=" + Arrays.toString(this.classes) + "]";
		}
	}
}
