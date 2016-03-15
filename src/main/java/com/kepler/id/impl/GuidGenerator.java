package com.kepler.id.impl;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.id.IDGenerator;
import com.kepler.protocol.Bytes;

public class GuidGenerator implements IDGenerator {

	private static final String NAME = "guid";

	private int time;

	private int machineId;

	private int processId;

	private AtomicInteger inc = new AtomicInteger(0);

	public GuidGenerator() {
		setMachineId();
		setProcessId();
	}

	private void setMachineId() {
		try {
			StringBuilder sb = new StringBuilder();
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				sb.append(ni.toString());
			}
			machineId = sb.toString().hashCode() << 16;
		} catch (Throwable e) {
			machineId = (new Random().nextInt()) << 16;
		}
	}

	private void setProcessId() {
		int processId = new java.util.Random().nextInt();
		try {
			processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
		} catch (Throwable t) {
		}
		int loaderId = 0;
		ClassLoader loader = GuidGenerator.class.getClassLoader();
		if (loader != null) {
			loaderId = System.identityHashCode(loader);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(processId));
		sb.append(Integer.toHexString(loaderId));
		processId = Integer.toHexString(processId).hashCode() & 0xFFFF;
	}

	@Override
	public Bytes generate() {
		this.time = (int) (System.currentTimeMillis() / 1000);
		int inc = this.inc.getAndIncrement();
		byte b[] = new byte[12];
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.putInt(this.time);
		bb.putInt(this.machineId | this.processId);
		bb.putInt(inc);
		return new Bytes(b);
	}

	@Override
	public String name() {
		return GuidGenerator.NAME;
	}
	
}
