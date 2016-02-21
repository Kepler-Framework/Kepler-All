package com.kepler.router.routing;

import java.util.HashMap;
import java.util.Map;

import com.kepler.extension.Extension;
import com.kepler.router.Routing;

/**
 * @author zhangjiehao 2015年9月7日
 */
public class Routings implements Extension {

	private final Map<String, Routing> routings = new HashMap<String, Routing>();

	@Override
	public Routings install(Object instance) {
		Routing routing = Routing.class.cast(instance);
		this.routings.put(routing.name(), routing);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Routing.class;
	}

	public Routing get(String name) {
		return this.routings.get(name);
	}
}
