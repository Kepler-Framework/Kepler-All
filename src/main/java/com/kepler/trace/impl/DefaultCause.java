package com.kepler.trace.impl;

import com.kepler.service.Service;
import com.kepler.trace.TraceCause;

/**
 * @author KimShen
 *
 */
public class DefaultCause implements TraceCause {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Service service;

	private final String method;

	private final String trace;

	public DefaultCause(Service service, String method, String trace) {
		super();
		this.service = service;
		this.method = method;
		this.trace = trace;
	}

	@Override
	public Service service() {
		return this.service;
	}

	@Override
	public String method() {
		return this.method;
	}

	@Override
	public String trace() {
		return this.trace;
	}
}
