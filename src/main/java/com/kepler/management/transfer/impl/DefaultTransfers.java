package com.kepler.management.transfer.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Status;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.management.transfer.Transfer;
import com.kepler.management.transfer.Transfers;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月24日
 */
public class DefaultTransfers implements Transfers {

	private final static long serialVersionUID = 1L;

	private final static Log LOGGER = LogFactory.getLog(DefaultTransfers.class);

	/**
	 * 冻结状态阀值
	 */
	private final static int FREEZE = PropertiesUtils.get(DefaultTransfers.class.getName().toLowerCase() + ".freeze", 5);

	/**
	 * 需要冻结的Transfer
	 */
	private final Collection<Transfer> removed = new HashSet<Transfer>();

	/**
	 * 当前DefaultTransfers唯一ID(用于Log)
	 */
	private final String uuid = UUID.randomUUID().toString();

	private final MultiKeyMap transfers = new MultiKeyMap();

	private final String service;

	private final String version;

	private final String method;

	public DefaultTransfers(Service service, String method) {
		super();
		this.method = method;
		this.service = service.service().getName();
		this.version = service.versionAndCatalog();
	}

	/**
	 * Remove or clear
	 * 
	 * @param transfer
	 */
	private void clear(WriteableTransfer transfer) {
		// 当前WriteableTransfer是否需要冻结并移除
		if (transfer.freezed()) {
			this.removed.add(transfer);
		} else {
			// 重置
			transfer.reset();
		}
	}

	/**
	 * 清理过期Transfer
	 */
	private void remove() {
		Iterator<Transfer> removed = this.removed.iterator();
		while (removed.hasNext()) {
			this.remove(removed.next());
			removed.remove();
		}
	}

	private void remove(Transfer each) {
		Transfer removed = Transfer.class.cast(this.transfers.remove(each.local(), each.target()));
		if (removed != null) {
			DefaultTransfers.LOGGER.warn("Transfer: (" + removed.local() + ") to (" + removed.target() + ") removed ... (" + this + ")");
		}
	}

	public String service() {
		return this.service;
	}

	public String version() {
		return this.version;
	}

	public String method() {
		return this.method;
	}

	@SuppressWarnings("unchecked")
	public Collection<Transfer> transfers() {
		return this.transfers.values();
	}

	public void clear() {
		// 标记,清理
		for (Object transfer : this.transfers.values()) {
			this.clear(WriteableTransfer.class.cast(transfer));
		}
		this.remove();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public Transfer get(Host local, Host target) {
		return Transfer.class.cast(this.transfers.get(local, target));
	}

	// 并发情况下出现允许范围内的丢失 (首次初始化时)
	public Transfer put(Host local, Host target, Status status, long rtt) {
		WriteableTransfer transfer = WriteableTransfer.class.cast(this.transfers.get(local, target));
		// 不存在则创建
		this.transfers.put(local, target, (transfer = (transfer != null ? transfer : new WriteableTransfer(local, target))).touch().rtt(rtt).timeout(status).exception(status));
		return transfer;
	}

	private class WriteableTransfer implements Transfer {

		private final static long serialVersionUID = 1L;

		private final AtomicLong rtt = new AtomicLong();

		private final AtomicLong total = new AtomicLong();

		private final AtomicLong freeze = new AtomicLong();

		private final AtomicLong timeout = new AtomicLong();

		private final AtomicLong exception = new AtomicLong();

		private final Host local;

		private final Host target;

		public WriteableTransfer(Host local, Host target) {
			super();
			this.local = local;
			this.target = target;
			DefaultTransfers.LOGGER.warn("WriteableTransfer (" + DefaultTransfers.this.uuid + ") created: " + local + " / " + target + ") for (" + DefaultTransfers.this.service() + " / " + DefaultTransfers.this.version() + ")");
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

		public WriteableTransfer touch() {
			this.total.incrementAndGet();
			return this;
		}

		public WriteableTransfer rtt(long rtt) {
			this.rtt.addAndGet(rtt);
			return this;
		}

		public WriteableTransfer timeout(Status status) {
			if (status.equals(Status.TIMEOUT)) {
				this.timeout.incrementAndGet();
			}
			return this;
		}

		public WriteableTransfer exception(Status status) {
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
		}

		/**
		 * 如果连续N次没有请求量则进入冻结状态,用于主机永久性离线后的WriteableTransfer清理
		 * 
		 * @return
		 */
		public boolean freezed() {
			return this.total.get() == 0 ? this.freeze.incrementAndGet() > DefaultTransfers.FREEZE : this.warm();
		}

		/**
		 * 激活
		 * 
		 * @return
		 */
		private boolean warm() {
			this.freeze.set(0);
			return false;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}