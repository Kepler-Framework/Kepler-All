package com.kepler.ack;

import com.kepler.channel.ChannelInvoker;

/**
 * ACK超时处理
 * 
 * @author kim
 *
 * 2016年2月9日
 */
public interface AckTimeOut {

	/**
	 * @param service
	 * @param method
	 * @param invoker 
	 * @param times 当前周期ACK次数
	 */
	public void timeout(ChannelInvoker invoker, Ack ack, long times);
}
