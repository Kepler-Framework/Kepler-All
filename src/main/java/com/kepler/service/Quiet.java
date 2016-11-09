package com.kepler.service;

import com.kepler.protocol.Request;

/**
 * 静默服务列表
 * 
 * @author kim 2015年12月15日
 */
public interface Quiet {

	/**
	 * 是否为静默异常
	 * 
	 * @param request
	 * @param throwable
	 * @return
	 */
	public boolean quiet(Request request, Class<? extends Throwable> throwable);

	/**
	 * 如果为非静默异常则输出日志
	 * 
	 * @param request
	 * @param throwable
	 */
	public boolean print(Request request, Throwable throwable);
}
