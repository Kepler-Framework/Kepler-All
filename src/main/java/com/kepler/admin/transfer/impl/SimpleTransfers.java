package com.kepler.admin.transfer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.kepler.ack.Status;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author KimShen
 *
 */
public class SimpleTransfers implements Transfers {

	private static final long serialVersionUID = 1L;

	private final List<Transfer> transfers = new ArrayList<Transfer>();

	private final String service;

	private final String version;

	private final String method;

	public SimpleTransfers(String service, String version, String method, Collection<Transfer> transfers) {
		super();
		this.service = service;
		this.version = version;
		this.method = method;
		this.transfers(transfers);
	}

	/**
	 * 加载有效Transfer
	 * 
	 * @param transfers
	 */
	private void transfers(Collection<Transfer> transfers) {
		for (Transfer each : transfers) {
			if (each.actived()) {
				this.transfers.add(each);
			}
		}
	}

	@Override
	public String service() {
		return this.service;
	}

	@Override
	public String version() {
		return this.version;
	}

	@Override
	public String method() {
		return this.method;
	}

	@Override
	public void clear() {
		this.transfers.clear();
	}

	@Override
	public void reset() {
		for (Transfer each : this.transfers) {
			each.reset();
		}
	}

	/**
	 * 是否为激活状态(至少存在1个Transfer)
	 * 
	 * @return
	 */
	public boolean actived() {
		return !this.transfers.isEmpty();
	}

	@Override
	public Collection<Transfer> transfers() {
		return this.transfers;
	}

	@Override
	public Transfer get(Host local, Host target) {
		for (Transfer each : this.transfers) {
			// 主机相等
			if (each.local().equals(local) && each.target().equals(target)) {
				return each;
			}
		}
		return null;
	}

	@Override
	public Transfer put(Host local, Host target, Status status, long rtt) {
		// 只读, 禁止修改
		return this.get(local, target);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
