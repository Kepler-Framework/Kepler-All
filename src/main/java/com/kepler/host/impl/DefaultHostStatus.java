package com.kepler.host.impl;

import java.util.Map;

import com.kepler.host.Host;
import com.kepler.host.HostStatus;

/**
 * @author kim
 *
 * 2016年3月7日
 */
public class DefaultHostStatus implements HostStatus {

	private static final long serialVersionUID = 1L;

	private final String sid;

	private final String pid;

	private final String host;

	private final String group;

	public final Map<String, Object> status;

	public DefaultHostStatus(Host host, Map<String, Object> status) {
		super();
		this.sid = host.sid();
		this.pid = host.pid();
		this.host = host.host();
		this.group = host.group();
		this.status = status;
	}

	@Override
	public String getSid() {
		return this.sid;
	}

	@Override
	public String getPid() {
		return this.pid;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public Map<String, Object> getStatus() {
		return this.status;
	}
}
