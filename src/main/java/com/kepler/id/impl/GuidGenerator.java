package com.kepler.id.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.impl.ServerHost;
import com.kepler.id.IDGenerator;
import com.kepler.main.Pid;

/**
 * @author zhangjiehao
 *
 * 2016年3月16日
 */
public class GuidGenerator implements IDGenerator {

	/**
	 * 用以Hex
	 */
	private static final String ARRAY = PropertiesUtils.get(GuidGenerator.class.getName().toLowerCase() + ".array", "0123456789ABCDEF");

	private static final char[] HEX = GuidGenerator.ARRAY.toCharArray();

	private static final String NAME = "guid";

	private final AtomicInteger incr = new AtomicInteger(0);

	private int machine;

	private int pid;

	public GuidGenerator(int pid, int machine) {
		this.pid = pid;
		this.machine = machine;
	}

	public GuidGenerator(Pid pid, ServerHost host) {
		this.pid = Integer.valueOf(pid.pid());
		this.machine = host.sid().hashCode() << 16;
	}

	/**
	 * 写入 Int 
	 * 
	 * @param bytes
	 * @param index
	 * @param data
	 * @return 当前索引
	 */
	private int write(byte[] bytes, int index, int data) {
		bytes[(index++)] = (byte) (0xff & data);
		bytes[(index++)] = (byte) ((0xff00 & data) >> 8);
		bytes[(index++)] = (byte) ((0xff0000 & data) >> 16);
		bytes[(index++)] = (byte) ((0xff000000 & data) >> 24);
		return index;
	}

	public String toString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = GuidGenerator.HEX[v >>> 4];
			hexChars[j * 2 + 1] = GuidGenerator.HEX[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public byte[] generate() {
		byte bytes[] = new byte[12];
		// 写入时间相关信息
		int offset = 0;
		offset = this.write(bytes, offset, (int) (System.currentTimeMillis() / 1000));
		// 写入机器相关信息
		offset = this.write(bytes, offset, this.machine | this.pid);
		// 写入自增信息
		this.write(bytes, offset, this.incr.incrementAndGet());
		return bytes;
	}

	@Override
	public String name() {
		return GuidGenerator.NAME;
	}
}
