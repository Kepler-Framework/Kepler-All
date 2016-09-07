package com.kepler.trace;

import java.util.List;

import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface TraceCauses {

	/**
	 * @param request 
	 */
	public void put(Request request, Throwable throwable);

	/**
	 * 获取并重置
	 * 
	 * @return
	 */
	public List<TraceCause> get();
}
