package com.kepler.generic.reflect.impl;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.generic.reflect.GenericArgs;

/**
 * 参数代理
 * 
 * @author KimShen
 *
 */
public class DelegateArgs implements GenericArgs {

	/**
	 * 原生类型映射
	 */
	private static final Map<String, Class<?>> PRIMITIVE = new HashMap<String, Class<?>>();

	private static final Class<?>[] EMPTY_CLASS = new Class<?>[0];

	private static final Object[] EMPTY_OBJECT = new Object[0];

	private static final long serialVersionUID = 1L;

	static {
		DelegateArgs.PRIMITIVE.put(int.class.getName(), int.class);
		DelegateArgs.PRIMITIVE.put(long.class.getName(), long.class);
		DelegateArgs.PRIMITIVE.put(byte.class.getName(), byte.class);
		DelegateArgs.PRIMITIVE.put(float.class.getName(), float.class);
		DelegateArgs.PRIMITIVE.put(short.class.getName(), short.class);
		DelegateArgs.PRIMITIVE.put(double.class.getName(), double.class);
		DelegateArgs.PRIMITIVE.put(boolean.class.getName(), boolean.class);
	}

	@JsonProperty
	private final String[] classes;

	@JsonProperty
	private final Object[] args;

	public DelegateArgs(@JsonProperty("classes") String[] classes, @JsonProperty("args") Object... args) {
		super();
		this.classes = classes;
		this.args = args;
	}

	public Class<?>[] classes() throws Exception {
		// Guard case
		if (this.classes == null) {
			return DelegateArgs.EMPTY_CLASS;
		}
		Class<?>[] classes = new Class<?>[this.classes.length];
		for (int index = 0; index < this.classes.length; index++) {
			classes[index] = DelegateArgs.PRIMITIVE.containsKey(this.classes[index]) ? DelegateArgs.PRIMITIVE.get(this.classes[index]) : Class.forName(this.classes[index]);
		}
		return classes;
	}

	public String[] classesAsString() {
		return this.classes;
	}

	public Object[] args() {
		return this.args == null ? DelegateArgs.EMPTY_OBJECT : this.args;
	}

	public boolean guess() {
		// 参数类型不存在并且参数数量不等于0
		return this.classes == null && this.args.length != 0;
	}
}
