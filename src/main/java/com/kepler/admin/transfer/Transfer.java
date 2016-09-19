package com.kepler.admin.transfer;

import java.io.Serializable;

import com.kepler.host.Host;

/**
 * 
 * @author kim 2015年7月24日
 */
public interface Transfer extends Serializable {
	
	public Host local();

	public Host target();

	/**
	 * Round trip time
	 * 
	 * @return
	 */
	public long rtt();

	/**
	 * 累计数量
	 * 
	 * @return
	 */
	public long total();
	
	/**
	 * 超时数量
	 * 
	 * @return
	 */
	public long timeout();

	/**
	 * 异常数量
	 * 
	 * @return
	 */
	public long exception();

	public void reset();
}
