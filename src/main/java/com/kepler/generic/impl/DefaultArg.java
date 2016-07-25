package com.kepler.generic.impl;

import com.kepler.generic.GenericArg;

/**
 * @author KimShen
 *
 */
public class DefaultArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	private final String clazz;

	private final Object arg;

	/**
	 * 参数类型为当前类型
	 * 
	 * @param arg
	 */
	public DefaultArg(Object arg) {
		this(arg, arg.getClass().getName());
	}

	/**
	 * @param arg 当前代理
	 * @param clazz 目标类型
	 */
	public DefaultArg(Object arg, String clazz) {
		super();
		this.clazz = clazz;
		this.arg = arg;
	}

	@Override
	public Class<?> clazz() throws Exception {
		return Class.forName(this.clazz);
	}

	@Override
	public Object arg() {
		return this.arg;
	}
}
