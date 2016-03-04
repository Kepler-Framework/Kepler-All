package com.kepler.router.routing;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangjiehao 2015年9月7日
 */
public class RoundRobinLoadBalance extends LoadBalance {

	public static final String NAME = "roundrobin";

	private final AtomicInteger indexes = new AtomicInteger(-1);

	@Override
	public String name() {
		return RoundRobinLoadBalance.NAME;
	}

	@Override
	protected int next(int weights) {
		return (this.indexes.incrementAndGet() & Byte.MAX_VALUE) % weights;
	}
}
