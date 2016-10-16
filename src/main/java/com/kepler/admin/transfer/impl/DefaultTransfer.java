package com.kepler.admin.transfer.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.kepler.ack.Status;
import com.kepler.admin.transfer.Transfer;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author KimShen
 *
 */
public class DefaultTransfer implements Transfer {

	/**
	 * 冻结状态阀值(127次默认)
	 */
	private static final int FREEZE = PropertiesUtils.get(DefaultTransfer.class.getName().toLowerCase() + ".freeze", Byte.MAX_VALUE);

	private static final long serialVersionUID = 1L;

	private final AtomicLong rtt = new AtomicLong();

	private final AtomicLong total = new AtomicLong();

	private final AtomicLong freeze = new AtomicLong();

	private final AtomicLong timeout = new AtomicLong();

	private final AtomicLong exception = new AtomicLong();

	private final Host target;

	private final Host local;

	/**
	 * 实际首次收集时间
	 */
	private long timestamp;

	public DefaultTransfer(Host local, Host target) {
		super();
		this.local = local;
		this.target = target;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * 如果连续N次没有请求量则进入冻结状态,用于主机永久性离线后的Transfer清理
	 * 
	 * @return
	 */
	public boolean freezed() {
		if (this.total.get() == 0) {
			return this.freeze.incrementAndGet() > DefaultTransfer.FREEZE;
		} else {
			// 任一一次请求有效则重置计数
			this.freeze.set(0);
			return false;
		}
	}

	@Override
	public Host local() {
		return this.local;
	}

	@Override
	public Host target() {
		return this.target;
	}

	public long rtt() {
		return this.rtt.get();
	}

	public long total() {
		return this.total.get();
	}

	public long timeout() {
		return this.timeout.get();
	}

	public long exception() {
		return this.exception.get();
	}

	public long timestamp() {
		return this.timestamp;
	}

	public boolean actived() {
		// 没有任何请求则标记为非激活
		return this.total.get() != 0;
	}

	public DefaultTransfer touch() {
		this.total.incrementAndGet();
		return this;
	}

	public DefaultTransfer rtt(long rtt) {
		if (rtt != 0) {
			this.rtt.addAndGet(rtt);
		}
		return this;
	}

	public DefaultTransfer timeout(Status status) {
		if (status.equals(Status.TIMEOUT)) {
			this.timeout.incrementAndGet();
		}
		return this;
	}

	public DefaultTransfer exception(Status status) {
		if (status.equals(Status.EXCEPTION)) {
			this.exception.incrementAndGet();
		}
		return this;
	}

	public void reset() {
		this.rtt.set(0);
		this.total.set(0);
		this.timeout.set(0);
		this.exception.set(0);
		// 更新时间
		this.timestamp = System.currentTimeMillis();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
