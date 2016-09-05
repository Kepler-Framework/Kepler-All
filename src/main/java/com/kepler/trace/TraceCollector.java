package com.kepler.trace;

import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface TraceCollector {

	/**
	 * @param request 
	 */
	public void put(Request request, Throwable throwable);

	/**
	 * 获取并重置
	 * 
	 * @return
	 */
	public TraceCause[] get();
}
