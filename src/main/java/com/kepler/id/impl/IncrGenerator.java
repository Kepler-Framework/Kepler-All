package com.kepler.id.impl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import com.kepler.id.IDGenerator;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public class IncrGenerator implements IDGenerator {

	public static final String NAME = "incr";

	private final AtomicLong next = new AtomicLong();

	@Override
	public Integer generate(Service service, Method method) {
		return this.generate(service, method.getName());
	}

	public Integer generate(Service service, String method) {
		return (int) (this.next.incrementAndGet() & Integer.MAX_VALUE);
	}

	public String name() {
		return IncrGenerator.NAME;
	}
}
