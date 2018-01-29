package com.kepler.router.filter.sid;

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
public class SIDFilter implements HostFilter {

	private static final boolean ENABLED = PropertiesUtils.get(SIDFilter.class.getName().toLowerCase() + ".enabled", false);

	private final SIDSelector selector;

	public SIDFilter(SIDSelector selector) {
		super();
		this.selector = selector;
	}

	@Override
	public List<Host> filter(Request request, List<Host> hosts) {
		return SIDFilter.ENABLED ? this.select(request, hosts) : hosts;
	}

	private List<Host> select(Request request, List<Host> hosts) {
		Host selected = this.selector.get(request.service());
		return selected != null ? Arrays.asList(selected) : hosts;
	}
}
