package com.kepler.channel;

import com.kepler.host.Host;

/**
 * @author kim 2015年7月9日
 */
public interface ChannelContext {

	/**
	 * 获取指定Host的ChannelInvoker
	 * 
	 * @param host
	 * @return
	 */
	public ChannelInvoker get(Host host);

	/**
	 * 删除指定Host的ChannelInvoker
	 * 
	 * @param host
	 * @return
	 */
	public ChannelInvoker del(Host host);

	/**
	 * 注册指定Host的ChannelInvoker
	 * 
	 * @param host
	 * @param invoker
	 * @return
	 */
	public ChannelInvoker put(Host host, ChannelInvoker invoker);

	/**
	 * 指定Host是否已注册ChannelInvoker
	 * 
	 * @param host
	 * @return
	 */
	public boolean contain(Host host);
}
