package com.kepler.generic.reflect.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.generic.reflect.GenericArgs;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.generic.reflect.GenericUtils;

/**
 * 参数代理
 * 
 * @author KimShen
 *
 */
public class DelegateArgs implements GenericArgs {

	private static final Class<?>[] EMPTY_CLASS = new Class<?>[0];

	private static final Object[] EMPTY_OBJECT = new Object[0];

	private static final long serialVersionUID = 1L;

	@JsonProperty
	private final String[] classes;

	@JsonProperty
	private final Object[] args;

	public DelegateArgs(GenericBean bean) {
		super();
		this.args = bean.args() != null ? bean.args().values().toArray(new Object[] {}) : DelegateArgs.EMPTY_OBJECT;
		this.classes = null;
	}

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
			classes[index] = GenericUtils.contains(this.classes[index]) ? GenericUtils.get(this.classes[index]) : Class.forName(this.classes[index]);
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
