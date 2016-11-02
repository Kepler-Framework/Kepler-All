package com.kepler.admin.transfer.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Status;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月24日
 */
public class DefaultTransfers implements Transfers {

	private static final Log LOGGER = LogFactory.getLog(DefaultTransfers.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Host(Local-Target) / Transfer
	 */
	private final ConcurrentMap<Hosts, Transfer> transfers = new ConcurrentHashMap<Hosts, Transfer>();

	private final Service service;

	private final String method;

	public DefaultTransfers(Service service, String method) {
		super();
		this.method = method;
		this.service = service;
	}

	public String service() {
		return this.service.service();
	}

	public String version() {
		return this.service.version();
	}

	public String method() {
		return this.method;
	}

	public Collection<Transfer> transfers() {
		return this.transfers.values();
	}

	public void clear() {
		for (Transfer transfer : this.transfers.values()) {
			// 如果为冻结状态则尝试移除
			if (DefaultTransfer.class.cast(transfer).freezed() && (this.transfers.remove(new Hosts(transfer.local(), transfer.target())) != null)) {
				DefaultTransfers.LOGGER.info("Clear transfer for " + this.service + " [method=" + this.method + "]");
			}
		}
	}

	public void reset() {
		for (Transfer transfer : this.transfers.values()) {
			transfer.reset();
		}
	}

	public Transfer get(Host local, Host target) {
		return this.transfers.get(new Hosts(local, target));
	}

	/**
	 * 获取或创建
	 * 
	 * @param local
	 * @param target
	 * @param transfer
	 * @return
	 */
	private Transfer get(Host local, Host target, Transfer transfer) {
		// 如果已存在则返回已存在否则返回新创建
		Transfer actual = this.transfers.putIfAbsent(new Hosts(local, target), transfer);
		return actual != null ? actual : transfer;
	}

	public Transfer put(Host local, Host target, Status status, long rtt) {
		Transfer transfer = this.transfers.get(new Hosts(local, target));
		transfer = (transfer != null ? transfer : this.get(local, target, new DefaultTransfer(local, target)));
		return DefaultTransfer.class.cast(transfer).touch().rtt(rtt).timeout(status).exception(status);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	private class Hosts {

		private Host local;

		private Host target;

		private Hosts() {
			super();
		}

		private Hosts(Host local, Host target) {
			this.local = local;
			this.target = target;
		}

		public int hashCode() {
			return this.local.hashCode() ^ this.target.hashCode();
		}

		public boolean equals(Object ob) {
			// Guard case
			if (ob == null) {
				return false;
			}
			Hosts host = Hosts.class.cast(ob);
			// 完全相等
			return this.local.equals(host.local) && this.target.equals(host.target);
		}
	}
}