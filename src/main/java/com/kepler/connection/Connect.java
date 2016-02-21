package com.kepler.connection;

import com.kepler.host.Host;

/**
 * @author kim 2015年7月8日
 */
public interface Connect {

	/**
	 * 建立连接
	 * 
	 * @param host
	 * @throws Exception
	 */
	public void connect(Host host) throws Exception;
}
