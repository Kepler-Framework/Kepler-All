package com.kepler.promotion;

import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年2月12日
 */
public interface Promotion {

	/**
	 * 本次请求是否提升
	 * 
	 * @param request
	 * @return
	 */
	public boolean promote(Request request);

	/**
	 * 统计
	 * 
	 * @param request
	 * @param start
	 */
	public void record(Request request, long start);
}
