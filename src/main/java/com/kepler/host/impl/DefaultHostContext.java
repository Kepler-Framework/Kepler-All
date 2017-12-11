package com.kepler.host.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerRoutingException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Connects;
import com.kepler.host.Host;
import com.kepler.host.Hosts;
import com.kepler.host.HostsContext;
import com.kepler.protocol.Request;
import com.kepler.router.Router;
import com.kepler.router.Routing;
import com.kepler.router.filter.HostFilter;
import com.kepler.router.routing.Routings;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月9日
 */
public class DefaultHostContext implements HostsContext, Router {

	public static final String ROUTING_KEY = DefaultHostContext.class.getName().toLowerCase() + ".routing";

	/**
	 * 默认路由策略
	 */
	private static final String ROUTING_DEF = PropertiesUtils.get(DefaultHostContext.ROUTING_KEY, Routing.NAME);

	private static final Log LOGGER = LogFactory.getLog(DefaultHostContext.class);

	/**
	 * 服务 - 主机映射
	 */
	private final Map<Service, Hosts> hosts = new HashMap<Service, Hosts>();

	private final HostFilter filter;

	private final Routings routings;

	/**
	 * 重连
	 */
	private final Connects connects;

	private final Profile profile;

	public DefaultHostContext(Connects connects, HostFilter filter, Profile profile, Routings routings) throws Exception {
		super();
		this.filter = filter;
		this.profile = profile;
		this.routings = routings;
		this.connects = connects;
	}

	private Hosts create(Service service, Hosts hosts) {
		// Hosts锁
		synchronized (this.hosts) {
			// Double check, 存在则返回, 不存在则Put
			if (this.hosts.containsKey(service)) {
				// Return Hosts
				return this.hosts.get(service);
			} else {
				this.hosts.put(service, hosts);
				return hosts;
			}
		}
	}

	public Hosts getOrCreate(Service service) {
		Hosts hosts = this.hosts.get(service);
		return hosts != null ? hosts : this.create(service, new DefaultHosts(service));
	}

	@Override
	public void ban(Host host) {
		synchronized (this.hosts) {
			boolean baned_all = false;
			for (Service service : this.hosts.keySet()) {
				Hosts hosts = this.hosts.get(service);
				// 强先后顺序, hosts.ban(host)必须调用
				boolean baned_each = hosts.ban(host);
				if (baned_each) {
					DefaultHostContext.LOGGER.info("Ban. [service=" + service + "][host=" + host.address() + "][pid=" + host.pid() + "]");
				}
				// 任何一台Host Ban成功则标记Baned = True.
				baned_all = baned_each || baned_all;
			}
			// Hosts中任意服务Ban成功均尝试重连
			if (baned_all) {
				this.connects.put(host);
			}
		}
	}

	public void active(Host host) {
		synchronized (this.hosts) {
			for (Service service : this.hosts.keySet()) {
				Hosts hosts = this.hosts.get(service);
				hosts.active(host);
				DefaultHostContext.LOGGER.info("Active. [service=" + service + "][host=" + host.address() + "][pid=" + host.pid() + "]");
			}
		}
	}

	public void remove(Host host, Service service) {
		synchronized (this.hosts) {
			Hosts hosts = this.hosts.get(service);
			hosts.remove(host);
			DefaultHostContext.LOGGER.info("Remove. [service=" + service + "][host=" + host.address() + "][pid=" + host.pid() + "]");
		}
	}

	// 只读(协商)
	public Map<Service, Hosts> hosts() {
		return this.hosts;
	}

	public Host host(Request request) {
		Routing routing = this.routings.get(PropertiesUtils.profile(this.profile.profile(request.service()), DefaultHostContext.ROUTING_KEY, DefaultHostContext.ROUTING_DEF));
		return routing.route(request, this.hosts(request));
	}

	// 只读(协商)
	public List<Host> hosts(Request request) {
		Hosts hosts = this.getOrCreate(request.service());
		// Request.header(Host.TAG_KEY, Host.TAG_DEF)), 获取Tag, 如果不存在则使用默认""
		String tag = request.get(Host.TAG_KEY, Host.TAG_DEF);
		List<Host> matched = hosts.tags(tag);
		// 若没有main, 也没有匹配tag, 则提前异常
		if (matched.isEmpty() && hosts.main().isEmpty()) {
			throw new KeplerRoutingException("None service for " + request.service() + " with tag " + tag);
		}
		// 获取Tag对应Host集合, 如果不存在则使用Main集合
		return this.valid(request, this.filter.filter(request, matched.isEmpty() ? hosts.main() : matched));
	}

	private List<Host> valid(Request request, List<Host> hosts) {
		if (hosts.isEmpty()) {
			throw new KeplerRoutingException("None service for " + request.service());
		}
		return hosts;
	}
}
