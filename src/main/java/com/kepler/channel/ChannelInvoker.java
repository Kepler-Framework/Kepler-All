package com.kepler.channel;

import com.kepler.host.Host;
import com.kepler.invoker.Invoker;

/**
 * @author kim 2015年7月9日
 */
public interface ChannelInvoker extends Invoker {

	/**
	 * 获取绑定Host
	 * 
	 * @return
	 */
	public Host host();

	/**
	 * 关闭通道
	 */
	public void close();

	/**
	 * 释放资源(异步)
	 */
	public void release();

	/**
	 * 释放资源(立即)
	 */
	public void releaseAtOnce();
}
