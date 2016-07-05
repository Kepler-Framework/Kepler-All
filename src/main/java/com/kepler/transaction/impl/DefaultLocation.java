package com.kepler.transaction.impl;

import com.kepler.transaction.Location;

/**
 * @author KimShen
 *
 */
public class DefaultLocation implements Location {

	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;

	private final String method;

	public DefaultLocation(Class<?> clazz, String method) {
		super();
		this.clazz = clazz;
		this.method = method;
	}

	@Override
	public String method() {
		return this.method;
	}

	@Override
	public Class<?> clazz() {
		return this.clazz;
	}

	public String toString() {
		return "[" + this.clazz() + "][" + this.method() + "]";
	}
}
