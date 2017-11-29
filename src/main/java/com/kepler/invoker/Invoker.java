package com.kepler.invoker;

import java.lang.reflect.Method;

import com.kepler.protocol.Request;

/**
 * @author kim 2015年7月8日
 */
public interface Invoker {

	public static final Object EMPTY = new Object();

	/**
	 * @param request
	 * @param method 原始方法, 可为空
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Request request, Method method) throws Throwable;

	/**
	 * 当前Invoker是否激活
	 * 
	 * @return
	 */
	public boolean actived();
}
