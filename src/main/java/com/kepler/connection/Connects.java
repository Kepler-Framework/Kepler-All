package com.kepler.connection;

import com.kepler.host.Host;

/**
 * 连接器(队列)
 * 
 * @author kim 2015年7月10日
 */
public interface Connects {

	public Host get() throws Exception;

	public void put(Host host);
}
