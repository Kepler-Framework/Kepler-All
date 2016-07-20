package com.kepler.transaction.impl;

import java.util.UUID;

import com.kepler.header.Headers;
import com.kepler.transaction.Location;
import com.kepler.transaction.Request;

/**
 * @author KimShen
 *
 */
public class DefaultRequest implements Request {

	private static final long serialVersionUID = 1L;

	/**
	 * 事务号
	 */
	private final String uuid = UUID.randomUUID().toString();

	private final Location location;

	private final Object[] args;

	private Headers headers;

	/**
	 * @param location 回滚入口
	 * @param args 参数集
	 */
	public DefaultRequest(Location location, Object... args) {
		super();
		this.location = location;
		this.args = args;
	}

	public DefaultRequest(Class<?> clazz, String method, Object... args) {
		super();
		this.location = new DefaultLocation(clazz, method);
		this.args = args;
	}

	public String uuid() {
		return this.uuid;
	}

	@Override
	public Object[] args() {
		return this.args;
	}

	@Override
	public Headers headers() {
		return this.headers;
	}

	@Override
	public DefaultRequest headers(Headers headers) {
		this.headers = headers;
		return this;
	}

	@Override
	public Location location() {
		return this.location;
	}
}
