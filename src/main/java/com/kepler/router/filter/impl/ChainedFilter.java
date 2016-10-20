package com.kepler.router.filter.impl;

import java.util.ArrayList;
import java.util.List;

import com.kepler.extension.Extension;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.filter.HostFilter;

/**
 * @author zhangjiehao 2015年9月7日
 */
public class ChainedFilter implements HostFilter, Extension {

	private final List<HostFilter> filters = new ArrayList<HostFilter>();

	@Override
	public ChainedFilter install(Object instance) {
		this.filters.add(HostFilter.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return HostFilter.class;
	}

	@Override
	public List<Host> filter(Request request, List<Host> hosts) {
		List<Host> approved = hosts;
		if (!this.filters.isEmpty()) {
			for (HostFilter filter : this.filters) {
				approved = filter.filter(request, approved);
			}
		}
		return approved;
	}
}
