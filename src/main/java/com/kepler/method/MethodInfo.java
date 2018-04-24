package com.kepler.method;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author KimShen
 *
 */
public class MethodInfo {

	private final Class<?>[] classes;

	private final String[] names;

	private final Method method;

	public MethodInfo(Class<?>[] classes, String[] names, Method method) {
		super();
		this.classes = classes;
		this.names = names;
		this.method = method;
	}

	public Object[] args(Map<String, Object> param) {
		if (this.names == null || this.names.length == 0) {
			return null;
		}
		Object[] args = new Object[this.names.length];
		for (int index = 0; index < this.names.length; index++) {
			args[index] = param.get(this.names[index]);
		}
		return args;
	}

	public Class<?>[] classes() {
		return this.classes;
	}

	public String[] names() {
		return this.names;
	}

	public Method method() {
		return this.method;
	}
}
