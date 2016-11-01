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
	 * @param invoker 通道 
	 * @param ack
	 * @param times 当前周期ACK次数
	 */
	public void timeout(ChannelInvoker invoker, Ack ack, long times);
}
