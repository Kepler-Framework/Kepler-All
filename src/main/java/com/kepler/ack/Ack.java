package com.kepler.ack;

import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author kim 2015年7月28日
 */
public interface Ack {

	/**
	 * 发起者(本地主机)
	 * 
	 * @return
	 */
	public Host local();

	/**
	 * 目标主机
	 * 
	 * @return
	 */
	public Host remote();

	/**
	 * 已耗时
	 * 
	 * @return
	 */
	public long elapse();

	/**
	 * ACK状态
	 * 
	 * @return
	 */
	public Status status();

	/**
	 * 原始请求
	 * 
	 * @return
	 */
	public Request request();
}
