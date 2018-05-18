package com.kepler.protocol.impl;

import java.lang.reflect.Method;

import com.kepler.header.Headers;
import com.kepler.header.impl.LazyHeaders;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.protocol.Request;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月14日
 */
public class DefaultRequest implements Request {

	private static final long serialVersionUID = 1L;

	private final Class<?>[] types;

	private final Service service;

	private final Headers headers;

	private final String method;

	private final Object[] args;

	/**
	 * 是否为异步请求
	 */
	private final boolean async;

	private final byte serial;

	private final byte[] ack;

	public DefaultRequest(Request request, byte[] ack) {
		this(ack, DefaultRequest.clone(request.headers()), request.service(), request.method(), request.async(), request.args(), request.types(), request.serial());
	}

	public DefaultRequest(Request request, byte[] ack, boolean async) {
		this(ack, DefaultRequest.clone(request.headers()), request.service(), request.method(), async, request.args(), request.types(), request.serial());
	}

	public DefaultRequest(Request request, byte[] ack, Object[] args) {
		this(ack, DefaultRequest.clone(request.headers()), request.service(), request.method(), request.async(), args, request.types(), request.serial());
	}

	public DefaultRequest(byte[] ack, Headers headers, Service service, Method method, boolean async, Object[] args, byte serial) {
		this(ack, headers, service, method.getName(), async, args, method.getParameterTypes(), serial);
	}

	public DefaultRequest(byte[] ack, Headers headers, Service service, String method, boolean async, Object[] args, Class<?>[] types, byte serial) {
		super();
		this.service = service;
		this.headers = headers;
		this.method = method;
		this.serial = serial;
		this.async = async;
		this.types = types;
		this.args = args;
		this.ack = ack;
	}

	/**
	 * 复制Headers
	 * 
	 * @param headers
	 * @return
	 */
	protected static Headers clone(Headers headers) {
		return Headers.ENABLED ? new LazyHeaders(headers.get()) : null;
	}

	public Service service() {
		return this.service;
	}

	public Class<?>[] types() {
		return this.types;
	}

	public String method() {
		return this.method;
	}

	public boolean async() {
		return this.async;
	}
	
	public Object[] args() {
		return this.args;
	}

	public byte serial() {
		return this.serial;
	}

	public byte[] ack() {
		return this.ack;
	}

	@Override
	public Headers headers() {
		return this.headers;
	}

	public String get(String key) {
		return this.headers == null ? null : this.headers.get(key);
	}

	public String get(String key, String def) {
		return this.headers == null ? def : this.headers.get(key, def);
	}

	@Override
	public DefaultRequest put(String key, String value) {
		if (this.headers != null) {
			this.headers.put(key, value);
		}
		return this;
	}

	@Override
	public DefaultRequest putIfAbsent(String key, String value) {
		if (this.headers != null) {
			this.headers.putIfAbsent(key, value);
		}
		return this;
	}
	
	@Override
	public DefaultRequest put(String key, Object value) {
		return this.put(key, value.toString());
	}

	@Override
	public DefaultRequest putIfAbsent(String key, Object value) {
		return this.putIfAbsent(key, value.toString());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}