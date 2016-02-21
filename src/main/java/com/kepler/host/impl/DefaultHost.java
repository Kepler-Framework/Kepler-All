package com.kepler.host.impl;

import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author kim 2015年7月8日
 */
public class DefaultHost implements Host {

	private final static long serialVersionUID = 1L;

	private final int port;

	private final int priority;

	private final String pid;

	private final String tag;

	private final String host;

	private final String token;

	private final String group;

	public DefaultHost(String group, String token, String tag, String pid, String host, int port, int priority) {
		this.tag = tag;
		this.pid = pid;
		this.host = host;
		this.port = port;
		this.token = token;
		this.group = group;
		this.priority = priority;
	}

	public String address() {
		return this.host() + ":" + this.port();
	}

	@Override
	public int priority() {
		return this.priority;
	}

	@Override
	public String group() {
		return this.group;
	}

	public String token() {
		return this.token;
	}

	@Override
	public String host() {
		return this.host;
	}

	@Override
	public String tag() {
		return this.tag;
	}

	public String sid() {
		// DefaultHost.sid = null
		return null;
	}

	@Override
	public String pid() {
		return this.pid;
	}

	@Override
	public int port() {
		return this.port;
	}

	public boolean loop(String host) {
		return this.host().equalsIgnoreCase(host);
	}

	public boolean loop(Host host) {
		return this.host().equalsIgnoreCase(host.host());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int hashCode() {
		return this.host().hashCode() ^ this.pid().hashCode() ^ this.port();
	}

	public boolean equals(Object ob) {
		// Not null point security
		Host host = Host.class.cast(ob);
		return this.host().equals(host.host()) && (this.pid().equals(host.pid())) && (this.port() == host.port());
	}
}