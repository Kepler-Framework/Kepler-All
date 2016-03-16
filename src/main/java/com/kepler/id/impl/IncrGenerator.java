package com.kepler.id.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.id.IDGenerator;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public class IncrGenerator implements IDGenerator {

	public static final String NAME = "incr";

	private final AtomicLong next = new AtomicLong();

	@Override
	public byte[] generate() {
		int next = (int) (this.next.incrementAndGet() & Integer.MAX_VALUE);
		byte b1 = (byte)((next >>> 24) & 0xFF);
		byte b2 = (byte)((next >>> 16) & 0xFF);
		byte b3 = (byte)((next >>> 8) & 0xFF);
		byte b4 = (byte)((next >>> 0) & 0xFF);
		return new byte[] { b1, b2, b3, b4 };
	}

	public String name() {
		return IncrGenerator.NAME;
	}
}
