package com.kepler.trace;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface TraceCollector {

	/**
	 * @param service 服务
	 * @param method  方法
	 */
	public void put(Service service, String method);

	/**
	 * 获取并重置
	 * 
	 * @return
	 */
	public TraceCause[] get();
}
