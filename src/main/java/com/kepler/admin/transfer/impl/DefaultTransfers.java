package com.kepler.admin.transfer.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Status;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月24日
 */
public class DefaultTransfers implements Transfers {

	private static final long serialVersionUID = 1L;

	/**
	 * 冻结状态阀值(127次默认)
	 */
	private static final int FREEZE = PropertiesUtils.get(DefaultTransfers.class.getName().toLowerCase() + ".freeze", Byte.MAX_VALUE);

	private static final Log LOGGER = LogFactory.getLog(DefaultTransfers.class);

	/**
	 * 可复用Hosts
	 */
	private static final ThreadLocal<Hosts> HOSTS = new ThreadLocal<Hosts>() {
		protected Hosts initialValue() {
			return new Hosts();
		}
	};

	private final ConcurrentMap<Hosts, Transfer> transfers = new ConcurrentHashMap<Hosts, Transfer>();

	private final String service;

	private final String version;

	private final String method;

	/**
	 * 是否处于激活状态, 仅激活状态的Transfers需要传输
	 */
	volatile private boolean actived;

	public DefaultTransfers(Service service, String method) {
		super();
		this.actived = false;
		this.method = method;
		this.service = service.service();
		this.version = service.versionAndCatalog();
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

	public boolean actived() {
		return this.actived;
	}

	public Collection<Transfer> transfers() {
		return this.transfers.values();
	}

	public void clear() {
		for (Transfer transfer : this.transfers.values()) {
			// 如果为冻结状态则进行移除
			if (WriteableTransfer.class.cast(transfer).freezed() && (this.transfers.remove(DefaultTransfers.HOSTS.get().reset(transfer.local(), transfer.target())) != null)) {
				DefaultTransfers.LOGGER.debug("Transfer: (" + transfer.local() + ") to (" + transfer.target() + ") removed ... (" + this + ")");
			}
		}
	}

	public void reset() {
		for (Transfer transfer : this.transfers.values()) {
			transfer.reset();
		}
		// 恢复状态为非激活
		this.actived = false;
	}

	public Transfer get(Host local, Host target) {
		return this.transfers.get(DefaultTransfers.HOSTS.get().reset(local, target));
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
		Transfer actual = this.transfers.putIfAbsent(DefaultTransfers.HOSTS.get().reset(local, target), transfer);
		return actual != null ? actual : transfer;
	}

	public Transfer put(Host local, Host target, Status status, long rtt) {
		Transfer transfer = this.transfers.get(DefaultTransfers.HOSTS.get().reset(local, target));
		transfer = (transfer != null ? transfer : this.get(local, target, new WriteableTransfer(local, target)));
		return WriteableTransfer.class.cast(transfer).touch().rtt(rtt).timeout(status).exception(status);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	private class WriteableTransfer implements Transfer {

		private static final long serialVersionUID = 1L;

		private final AtomicLong rtt = new AtomicLong();

		private final AtomicLong total = new AtomicLong();

		private final AtomicLong freeze = new AtomicLong();

		private final AtomicLong timeout = new AtomicLong();

		private final AtomicLong exception = new AtomicLong();

		private final Host target;

		private final Host local;

		private long started;

		private WriteableTransfer(Host local, Host target) {
			super();
			this.local = local;
			this.target = target;
			DefaultTransfers.LOGGER.debug("WriteableTransfer created: " + local + " to " + target + ") for (" + DefaultTransfers.this.service() + " / " + DefaultTransfers.this.version() + ")");
		}

		/**
		 * 如果连续N次没有请求量则进入冻结状态,用于主机永久性离线后的WriteableTransfer清理
		 * 
		 * @return
		 */
		public boolean freezed() {
			if (this.total.get() == 0) {
				return this.freeze.incrementAndGet() > DefaultTransfers.FREEZE;
			} else {
				// 重置
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

		public long started() {
			return this.started;
		}

		public long timeout() {
			return this.timeout.get();
		}

		public long exception() {
			return this.exception.get();
		}

		public WriteableTransfer touch() {
			DefaultTransfers.this.actived = true;
			this.total.incrementAndGet();
			return this;
		}

		public WriteableTransfer rtt(long rtt) {
			if (rtt != 0) {
				this.rtt.addAndGet(rtt);
			}
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
			this.started = System.currentTimeMillis();
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	private static class Hosts {

		private Host local;

		private Host target;

		private Hosts() {
			super();
		}

		public Hosts reset(Host local, Host target) {
			this.local = local;
			this.target = target;
			return this;
		}

		public int hashCode() {
			return this.local.hashCode() ^ this.target.hashCode();
		}

		public boolean equals(Object ob) {
			Hosts host = Hosts.class.cast(ob);
			return this.local.equals(host.local) && this.target.equals(host.target);
		}
	}
}