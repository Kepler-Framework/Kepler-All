package com.kepler.protocol.impl;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.header.Headers;
import com.kepler.protocol.Request;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月14日
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public class JacksonRequest extends DefaultRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JacksonRequest(Request request, byte[] ack) {
		super(ack, JacksonRequest.clone(request.headers()), request.service(), request.method(), request.async(), request.args(), request.types(), request.serial());
	}

	public JacksonRequest(Request request, byte[] ack, boolean async) {
		super(ack, JacksonRequest.clone(request.headers()), request.service(), request.method(), async, request.args(), request.types(), request.serial());
	}

	public JacksonRequest(Request request, byte[] ack, Object[] args) {
		super(ack, JacksonRequest.clone(request.headers()), request.service(), request.method(), request.async(), args, request.types(), request.serial());
	}

	public JacksonRequest(byte[] ack, Headers headers, Service service, Method method, boolean async, Object[] args, byte serial) {
		super(ack, headers, service, method.getName(), async, args, method.getParameterTypes(), serial);
	}

	public JacksonRequest(@JsonProperty("ack") byte[] ack, @JsonProperty("headers") Headers headers, @JsonProperty("service") Service service, @JsonProperty("method") String method, @JsonProperty("async") boolean async, @JsonProperty("args") Object[] args, @JsonProperty("types") Class<?>[] types, @JsonProperty("serial") byte serial) {
		super(ack, headers, service, method, async, args, types, serial);
	}

	@JsonProperty
	public Service service() {
		return super.service();
	}

	@JsonProperty
	public String method() {
		return super.method();
	}

	@JsonProperty
	public Class<?>[] types() {
		return super.types();
	}

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object[] args() {
		return super.args();
	}

	@JsonProperty
	public byte[] ack() {
		return super.ack();
	}

	@JsonProperty
	public Headers headers() {
		return super.headers();
	}
}