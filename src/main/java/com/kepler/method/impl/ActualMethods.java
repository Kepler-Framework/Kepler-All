package com.kepler.method.impl;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import com.kepler.method.MethodInfo;
import com.kepler.method.Methods;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class ActualMethods implements Methods {

	private final LocalVariableTableParameterNameDiscoverer discorer = new LocalVariableTableParameterNameDiscoverer();

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, Class<?>[] classes) throws Exception {
		Method actual = MethodUtils.getMatchingAccessibleMethod(service, method, classes);
		if (actual == null) {
			throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[classes=" + Arrays.toString(classes) + "]");
		}
		return new MethodInfo(actual.getParameterTypes(), this.discorer.getParameterNames(actual), actual);
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, String[] names) throws Exception {
		// 尝试遍历方法
		for (Method actual : service.getMethods()) {
			String[] params_source = this.discorer.getParameterNames(actual);
			String[] params_sorted = new String[params_source.length];
			System.arraycopy(params_source, 0, params_sorted, 0, params_source.length);
			Arrays.sort(params_sorted, String.CASE_INSENSITIVE_ORDER);
			Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
			if (Arrays.equals(params_sorted, names)) {
				return new MethodInfo(actual.getParameterTypes(), params_source, actual);
			}
		}
		throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[names=" + Arrays.toString(names) + "]");
	}

	@Override
	public MethodInfo method(Class<? extends Object> service, String method, int size) throws Exception {
		// 尝试遍历方法
		for (Method actual : service.getMethods()) {
			// 名称相等并且参数类型相等则判定相同
			if (actual.getName().equals(method) && actual.getParameterTypes().length == size) {
				return new MethodInfo(actual.getParameterTypes(), this.discorer.getParameterNames(actual), actual);
			}
		}
		throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[size=" + size + "]");
	}
}
