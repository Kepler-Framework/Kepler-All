package com.kepler.router.filter.impl;

import java.util.Arrays;
import java.util.List;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.filter.HostFilter;

/**
 * 定向过滤
 * 
 * @author kim 2015年11月24日
 */
public class AssumedFilter implements HostFilter {

	private final static boolean ENABLED = PropertiesUtils.get(AssumedFilter.class.getName().toLowerCase() + ".enabled", false);

	private final AssumedSelector selector;

	public AssumedFilter(AssumedSelector selector) {
		super();
		this.selector = selector;
	}

	@Override
	public List<Host> filter(Request request, List<Host> hosts) {
		return AssumedFilter.ENABLED ? this.assumed(request, hosts) : hosts;
	}

	private List<Host> assumed(Request request, List<Host> hosts) {
		Host assumed = this.selector.release(request.service());
		return assumed != null ? Arrays.asList(assumed) : hosts;
	}
}
