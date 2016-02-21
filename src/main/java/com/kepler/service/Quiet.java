package com.kepler.service;

import com.kepler.protocol.Request;

/**
 * 静默服务列表
 * 
 * @author kim 2015年12月15日
 */
public interface Quiet {

	public boolean quiet(Request request, Class<? extends Throwable> throwable);
}
