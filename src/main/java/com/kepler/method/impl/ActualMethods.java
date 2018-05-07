package com.kepler.method.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import com.kepler.advised.AdvisedFinder;
import com.kepler.config.PropertiesUtils;
import com.kepler.method.MethodInfo;
import com.kepler.method.Methods;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class ActualMethods implements Methods {

	private static final LocalVariableTableParameterNameDiscoverer DISCOVER = new LocalVariableTableParameterNameDiscoverer();

	private static final int MATCHED = PropertiesUtils.get(ActualMethods.class.getName().toLowerCase() + ".matched", 0);

	private static final Set<Class<?>> NOT_MATCHED = new HashSet<Class<?>>();

	private static final Log LOGGER = LogFactory.getLog(ActualMethods.class);

	static {
		ActualMethods.NOT_MATCHED.add(boolean.class);
		ActualMethods.NOT_MATCHED.add(double.class);
		ActualMethods.NOT_MATCHED.add(short.class);
		ActualMethods.NOT_MATCHED.add(float.class);
		ActualMethods.NOT_MATCHED.add(long.class);
		ActualMethods.NOT_MATCHED.add(byte.class);
		ActualMethods.NOT_MATCHED.add(int.class);
	}

	private final MethodComparator comparator = new MethodComparator();

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, Class<?>[] classes) throws Exception {
		Method actual = MethodUtils.getMatchingAccessibleMethod(service, method, classes);
		if (actual == null) {
			throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[classes=" + Arrays.toString(classes) + "]");
		}
		return new MethodInfo(actual.getParameterTypes(), ActualMethods.DISCOVER.getParameterNames(actual), actual);
	}

	@Override
	public MethodInfo method(Object instance, String method, String[] names) throws Exception {
		List<MethodNames> methods = new ArrayList<MethodNames>();
		Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
		// 迭代服务实例方法(非Proxy)
		for (Method actual : AdvisedFinder.actual4class(instance).getMethods()) {
			if (actual.getName().equals(method)) {
				methods.add(new MethodNames(MethodUtils.getAccessibleMethod(instance.getClass(), actual.getName(), actual.getParameterTypes()), actual, names));
			}
		}
		for (MethodNames each : methods) {
			// 返回首个参数完全匹配Method
			if (each.equals()) {
				return each.method();
			}
		}
		// 如果没有完全匹配则继续判断单参Bean的匹配
		if (!methods.isEmpty()) {
			// 按匹配值排序
			Collections.sort(methods, this.comparator);
			if (methods.get(0).match() > ActualMethods.MATCHED) {
				// 使用包装MethodInfo
				return methods.get(0).method(true);
			}
		}
		throw new NoSuchMethodException("No such method: " + method + " for class: " + instance.getClass() + "[names=" + Arrays.toString(names) + "]");
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, int size) throws Exception {
		// 尝试遍历方法
		for (Method actual : service.getMethods()) {
			// 名称相等并且参数类型相等则判定相同
			if (actual.getName().equals(method) && actual.getParameterTypes().length == size) {
				return new MethodInfo(actual.getParameterTypes(), ActualMethods.DISCOVER.getParameterNames(actual), actual);
			}
		}
		throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[size=" + size + "]");
	}

	private class MethodNames {

		private final String[] names_method;

		private final String[] names_source;

		private final String[] names_sorted;

		private final Method method;

		private Integer match;

		private MethodNames(Method proxy, Method actual, String[] names) {
			super();
			// 获取Method签名和排序后签名
			this.names_method = ActualMethods.DISCOVER.getParameterNames(actual);
			this.names_sorted = new String[this.names_method.length];
			// 请求签名
			this.names_source = names;
			this.method = proxy;
			this.copy();
		}

		private void copy() {
			// 复制 names_method —> names_sorted
			System.arraycopy(this.names_method, 0, this.names_sorted, 0, this.names_method.length);
			Arrays.sort(this.names_sorted, String.CASE_INSENSITIVE_ORDER);
		}

		private void count() {
			// Guard case1, 如果参数多于或者少于1个则无效匹配
			if (this.method.getParameterTypes().length != 1) {
				this.match = 0;
				return;
			}
			Class<?> param = this.method.getParameterTypes()[0];
			// Guard case2, 如果为基础类型
			if (param.isPrimitive()) {
				this.match = 0;
				return;
			}
			// Guard case3, Map最高匹配
			if (Map.class.isAssignableFrom(param)) {
				this.match = Integer.MAX_VALUE;
				return;
			}
			try {
				for (PropertyDescriptor descriptor : Introspector.getBeanInfo(param).getPropertyDescriptors()) {
					// 存在Set并且名称匹配
					if (descriptor.getWriteMethod() != null && Arrays.binarySearch(this.names_source, descriptor.getName()) != -1) {
						this.match = this.match != null ? this.match++ : 1;
					}
				}
				// 没有任何匹配则指定为0
				this.match = this.match != null ? this.match : 0;
			} catch (Exception e) {
				ActualMethods.LOGGER.error(e.getMessage(), e);
				this.match = 0;
			}
		}

		public MethodInfo method(boolean wrapper) {
			return new MethodInfo(wrapper, this.method.getParameterTypes(), this.names_method, this.method);
		}

		public MethodInfo method() {
			return this.method(false);
		}

		public boolean equals() {
			return Arrays.equals(this.names_sorted, this.names_source);
		}

		public int match() {
			if (this.match == null) {
				this.count();
			}
			return this.match;
		}
	}

	private class MethodComparator implements Comparator<MethodNames> {

		public int compare(MethodNames m1, MethodNames m2) {
			if (m1.match() == m2.match()) {
				return 0;
			}
			return m1.match() > m2.match() ? -1 : 1;
		}
	}
}
