package com.kepler.protocol.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.protocol.Response;

/**
 * @author kim
 *
 * 2016年2月14日
 */
public class DefaultResponse implements Response {

	private static final long serialVersionUID = 1L;

	private final byte serial;

	private final byte[] ack;

	private Throwable throwable;
	
	private Object response;

	public DefaultResponse(byte serial, byte[] ack, Object response) {
		this(serial, ack, response, null);
	}

	public DefaultResponse(byte serial, byte[] ack, Throwable throwable) {
		this(serial, ack, null, throwable);
	}

	private DefaultResponse(@JsonProperty("serial") byte serial, @JsonProperty("ack") byte[] ack, @JsonProperty("respones") Object response, @JsonProperty("throwable") Throwable throwable) {
		super();
		this.ack = ack;
		this.serial = serial;
		this.response = response;
		this.throwable = throwable;
	}
	

	@Override
	public DefaultResponse resend(Throwable ob) {
		this.response = null;
		this.throwable = ob;
		return this;
	}

	public byte serial() {
		return this.serial;
	}

	@Override
	public byte[] ack() {
		return this.ack;
	}

	@Override
	public Object response() {
		return this.response;
	}

	public Throwable throwable() {
		return this.throwable;
	}

	public boolean valid() {
		return this.throwable() == null;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}