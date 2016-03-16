package com.kepler.id.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.host.impl.ServerHost;
import com.kepler.id.IDGenerator;
import com.kepler.main.Pid;

/**
 * @author zhangjiehao
 *
 * 2016年3月16日
 */
public class GuidGenerator implements IDGenerator {

	private static final String NAME = "guid";

	private final AtomicInteger incr = new AtomicInteger(0);

	private int machine;

	private int pid;

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
		bytes[index] = (byte) (0xff & data);
		bytes[(index++)] = (byte) ((0xff00 & data) >> 8);
		bytes[(index++)] = (byte) ((0xff0000 & data) >> 16);
		bytes[(index++)] = (byte) ((0xff000000 & data) >> 24);
		return index + 1;
	}

	@Override
	public byte[] generate() {
		byte bytes[] = new byte[12];
		// 写入时间相关信息
		int index_time = this.write(bytes, 0, (int) (System.currentTimeMillis() / 1000));
		// 写入机器相关信息
		int index_machine = this.write(bytes, index_time, this.machine | this.pid);
		// 写入自增信息
		this.write(bytes, index_machine, this.incr.incrementAndGet());
		return bytes;
	}

	@Override
	public String name() {
		return GuidGenerator.NAME;
	}
}
