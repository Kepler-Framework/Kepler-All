package com.kepler.trace.impl;

import com.kepler.KeplerGenericException;
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

	private final long timestamp = System.currentTimeMillis();

	private final Service service;

	private final String method;

	private final String trace;

	private final String cause;

	public DefaultCause(String cause, Service service, String method, String trace) {
		super();
		this.service = service;
		this.method = method;
		this.trace = trace;
		this.cause = cause;
	}

	public DefaultCause(Throwable throwable, Service service, String method, String trace) {
		super();
		this.cause = this.cause(throwable).getMessage();
		this.service = service;
		this.method = method;
		this.trace = trace;
	}

	private Throwable cause(Throwable throwable) {
		// Guard case1, null
		if (throwable == null) {
			return null;
		}
		// Guard case2, 泛化错误
		if (KeplerGenericException.class.equals(throwable.getClass())) {
			return KeplerGenericException.class.cast(throwable).clone();
		}
		// Example: throw null
		return (throwable == null || throwable.getCause() == null) ? throwable : this.cause(throwable.getCause());
	}

	public long timestamp() {
		return this.timestamp;
	}

	@Override
	public Service service() {
		return this.service;
	}

	@Override
	public String method() {
		return this.method;
	}

	public String cause() {
		return this.cause;
	}

	@Override
	public String trace() {
		return this.trace;
	}
}
