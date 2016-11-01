package com.kepler.channel.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerRoutingException;
import com.kepler.channel.ChannelContext;
import com.kepler.channel.ChannelInvoker;
import com.kepler.host.Host;

/**
 * Host - 通道映射
 * 
 * @author kim 2015年7月9日
 */
public class DefaultChannelContext implements ChannelContext {

	private final Map<Host, ChannelInvoker> channels = new HashMap<Host, ChannelInvoker>();

	/**
	 * 指定ChannelInvoker禁止为Null(必须存在)
	 * 
	 * @param host
	 * @param invoker
	 * @return
	 */
	private ChannelInvoker valid(Host host, ChannelInvoker invoker) {
		if (invoker == null) {
			throw new KeplerRoutingException("Empty invoker for " + host.address() + " ... ");
		}
		return invoker;
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		for (ChannelInvoker invoker : this.channels.values()) {
			// 释放所有ChannelInvoker资源
			invoker.releaseAtOnce();
		}
	}

	public ChannelInvoker get(Host host) {
		return this.valid(host, this.channels.get(host));
	}

	public ChannelInvoker del(Host host) {
		synchronized (this) {
			return this.channels.remove(host);
		}
	}

	public ChannelInvoker put(Host host, ChannelInvoker invoker) {
		synchronized (this) {
			this.channels.put(host, invoker);
		}
		return invoker;
	}

	public boolean contain(Host host) {
		// 强一致
		synchronized (this) {
			return this.channels.containsKey(host);
		}
	}
}
