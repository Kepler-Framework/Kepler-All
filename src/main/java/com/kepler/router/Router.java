package com.kepler.router;

import java.util.Collection;

import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author kim 2015年7月8日
 */
public interface Router {

	/**
	 * 获取指定Request执行主机
	 * 
	 * @param request
	 * @return
	 */
	public Host host(Request request);

	/**
	 * 获取指定Request所有主机
	 * 
	 * @param request
	 * @return
	 */
	public Collection<Host> hosts(Request request);
}
