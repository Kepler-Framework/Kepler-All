package com.kepler.invoker.forkjoin.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.extension.Extension;
import com.kepler.invoker.forkjoin.Forker;

/**
 * @author kim 2016年1月18日
 */
public class Forks implements Extension {

	private final Map<String, Forker> forks = new HashMap<String, Forker>();

	public Forker get(String name) {
		return this.forks.get(name);
	}

	@Override
	public Forks install(Object instance) {
		Forker fork = Forker.class.cast(instance);
		this.forks.put(fork.name(), fork);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Forker.class;
	}
}
