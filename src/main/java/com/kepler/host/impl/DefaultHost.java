package com.kepler.host.impl;

import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author kim 2015年7月8日
 */
public class DefaultHost implements Host {

	private static final long serialVersionUID = 1L;

	private final int port;

	private final int feature;

	private final int priority;

	private final String pid;

	private final String tag;

	private final String host;

	private final String name;

	private final String token;

	private final String group;

	private final String location;

	public DefaultHost(Host host) {
		this(host.location(), host.group(), host.token(), host.name(), host.tag(), host.pid(), host.host(), host.port(), host.feature(), host.priority());
	}

	public DefaultHost(String location, String group, String token, String name, String tag, String pid, String host, int port, int feature, int priority) {
		this.tag = tag;
		this.pid = pid;
		this.name = name;
		this.host = host;
		this.port = port;
		this.token = token;
		this.group = group;
		this.feature = feature;
		this.location = location;
		this.priority = priority;
	}

	public String address() {
		return this.host() + ":" + this.port();
	}

	public int feature() {
		return this.feature;
	}

	@Override
	public int priority() {
		return this.priority;
	}

	public String location() {
		return this.location;
	}

	@Override
	public String group() {
		return this.group;
	}

	public String token() {
		return this.token;
	}

	public String name() {
		return this.name;
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
		// Guard case
		if (ob == null) {
			return false;
		}
		Host host = Host.class.cast(ob);
		return this.host().equals(host.host()) && (this.pid().equals(host.pid())) && (this.port() == host.port());
	}
}