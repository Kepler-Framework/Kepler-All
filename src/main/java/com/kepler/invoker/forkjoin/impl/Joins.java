package com.kepler.invoker.forkjoin.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.extension.Extension;
import com.kepler.invoker.forkjoin.Joiner;

/**
 * @author kim 2016年1月16日
 */
public class Joins implements Extension {

	private final Map<String, Joiner> joins = new HashMap<String, Joiner>();

	public Joiner get(String name) {
		return this.joins.get(name);
	}

	@Override
	public Joins install(Object instance) {
		Joiner joiner = Joiner.class.cast(instance);
		this.joins.put(joiner.name(), joiner);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Joiner.class;
	}
}
