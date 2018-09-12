package com.kepler.host.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerLocalException;
import com.kepler.host.Host;
import com.kepler.host.HostState;
import com.kepler.host.Hosts;
import com.kepler.service.Service;

/**
 * @author kim 2015年12月30日
 */
public class DefaultHosts implements Hosts {

	private static final List<Host> EMPTY = Collections.unmodifiableList(new ArrayList<Host>());

	private static final Log LOGGER = LogFactory.getLog(DefaultHosts.class);

	/**
	 * 所有主机(所有状态)
	 */
	private final List<Host> hosts = new ArrayList<Host>();

	private final Set<Host> waiting = new HashSet<Host>();

	private final Set<Host> bans = new HashSet<Host>();

	private final Tags tags = new Tags();

	private final SIDs sids = new SIDs();

	private final Service service;

	public DefaultHosts(Service service) {
		super();
		this.service = service;
	}

	private String detail(Host host, String action) {
		return new StringBuffer().append("Host: ").append(host).append(") ").append(action).append(" ... (").append(this.service).append(") ").toString();
	}

	/**
	 * (任意状态)是否含指定Host, 调用者加锁
	 * 
	 * @param host
	 * @return
	 */
	public boolean contain(Host host) {
		return this.hosts.contains(host) || this.bans.contains(host) || this.waiting.contains(host);
	}

	private boolean remove4active(Host host) {
		boolean remove_address = this.sids.remove(host) != null;
		boolean remove_host = this.hosts.remove(host);
		boolean remove_tag = this.tags.remove(host);
		DefaultHosts.LOGGER.info("[remove-active][host=" + host + "][address=" + remove_address + "][host=" + remove_host + "][tag=" + remove_tag + "]");
		return remove_host && remove_tag && remove_address;
	}

	private boolean remove4wait(Host host) {
		boolean remove_wait = this.waiting.remove(host);
		DefaultHosts.LOGGER.info("[remove-wait][host=" + host + "][wait=" + remove_wait + "]");
		return remove_wait;
	}

	private boolean remove4ban(Host host) {
		boolean remove_ban = this.bans.remove(host);
		DefaultHosts.LOGGER.info("[remove-ban][host=" + host + "][ban=" + remove_ban + "]");
		return remove_ban;
	}

	public void remove(Host host) {
		synchronized (this) {
			// 从Host&&Tag&Address删除(运行时Host)或从Ban||Waiting删除(待连接Host)
			boolean remove4active = this.remove4active(host);
			boolean remove4wait = this.remove4wait(host);
			boolean remove4ban = this.remove4ban(host);
			if (remove4active || remove4wait || remove4ban) {
				DefaultHosts.LOGGER.warn(this.detail(host, "removed"));
			}
		}
	}

	public void wait(Host host) {
		synchronized (this) {
			// 不在任意列表
			if (!this.contain(host)) {
				this.waiting.add(host);
				DefaultHosts.LOGGER.warn(this.detail(host, "waiting"));
			}
		}
	}

	public void active(Host host) {
		synchronized (this) {
			// 从Ban&&Waiting(待连接Host)移除并加入到Tags&&Hosts&&Address(运行时Host)
			if (this.bans.remove(host) || this.waiting.remove(host)) {
				this.sids.put(host);
				this.tags.put(host);
				this.hosts.add(host);
				DefaultHosts.LOGGER.warn(this.detail(host, "active"));
			}
		}
	}

	public void replace(Host current, Host newone) {
		synchronized (this) {
			this.remove(current);
			this.sids.put(newone);
			this.tags.put(newone);
			this.hosts.add(newone);
			DefaultHosts.LOGGER.warn(this.detail(newone, "replace"));
		}
	}

	public boolean ban(Host host) {
		synchronized (this) {
			// 从Hosts&&Tags&Address移除或从Waiting(运行时Host)移除
			boolean removeActive = this.hosts.remove(host) && this.tags.remove(host);
			boolean removeWaiting = this.waiting.remove(host);
			if (removeActive || removeWaiting) {
				this.sids.remove(host);
				this.bans.add(host);
				DefaultHosts.LOGGER.warn(this.detail(host, "baned"));
				return true;
			}
			// 处理已在Ban队列中的Ban操作(如重连失败)
			return this.bans.contains(host);
		}
	}

	public List<Host> main() {
		return this.tags.get(Host.TAG_DEF);
	}

	public List<Host> tags(String tag) {
		return this.tags.get(tag);
	}

	public List<Host> select(HostState state) {
		switch (state) {
		case WAITING:
			return new ArrayList<Host>(this.waiting);
		case ACTIVE:
			return new ArrayList<Host>(this.hosts);
		case BAN:
			return new ArrayList<Host>(this.bans);
		default:
			throw new KeplerLocalException("Unvalid state for " + state);
		}
	}

	@Override
	public Host select(String sid) {
		return this.sids.get(sid);
	}

	public String toString() {
		return "[service=" + this.service + "][waiting=" + this.waiting.size() + "][ban=" + this.bans.size() + "][hosts=" + this.hosts.size() + "]";
	}

	private class Tags {

		private final Map<String, List<Host>> tags = new HashMap<String, List<Host>>();

		public List<Host> get(String tag) {
			for (String each : tag.split(Host.TAG_MULTI)) {
				List<Host> hosts = this.tags.get(each);
				if (hosts != null) {
					return hosts;
				}
			}
			return DefaultHosts.EMPTY;
		}

		/**
		 * 由外部调用进行同步安全
		 * 
		 * @param host
		 * @return
		 */
		public Tags put(Host host) {
			List<Host> hosts = this.get(host.tag());
			// 不存在则创建
			(hosts = hosts != DefaultHosts.EMPTY ? hosts : new ArrayList<Host>()).add(host);
			this.tags.put(host.tag(), hosts);
			return this;
		}

		/**
		 * 由外部调用进行同步安全
		 * 
		 * @param host
		 * @return
		 */
		public boolean remove(Host host) {
			List<Host> hosts = this.get(host.tag());
			// InvokerHandler.channelInactive回调此方法.
			// 禁止对DefaultHosts.EMPTY调用Remove
			return hosts != DefaultHosts.EMPTY ? hosts.remove(host) : false;
		}
	}

	private class SIDs {

		private final Map<String, Host> sid = new ConcurrentHashMap<String, Host>();

		public Host put(Host host) {
			return this.sid.put(host.sid(), host);
		}

		public Host get(String sid) {
			return this.sid.get(sid);
		}

		public Host remove(Host host) {
			// 对比当前Host, 如果一致则删除
			Host snapshot = this.sid.get(host.sid());
			Host removed = (snapshot != null) ? snapshot.equals(host) ? this.sid.remove(host.sid()) : null : null;
			DefaultHosts.this.detail(host, "sid removed");
			return removed;
		}
	}
}
