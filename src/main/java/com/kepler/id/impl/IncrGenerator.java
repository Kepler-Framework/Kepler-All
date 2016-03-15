package com.kepler.id.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import com.kepler.id.IDGenerator;
import com.kepler.protocol.Bytes;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public class IncrGenerator implements IDGenerator {

	public static final String NAME = "incr";

	private final AtomicLong next = new AtomicLong();

	@Override
	public Bytes generate() {
		byte b[] = new byte[32];
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.putInt((int) (this.next.incrementAndGet() & Integer.MAX_VALUE));
		return new Bytes(b);
	}

	public String name() {
		return IncrGenerator.NAME;
	}
}
