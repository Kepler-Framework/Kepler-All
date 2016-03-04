package com.kepler.invoker;

import com.kepler.protocol.Request;

/**
 * @author kim 2015年7月8日
 */
public interface Invoker {

	public static final Object EMPTY = new Object();

	public Object invoke(Request request) throws Throwable;

	/**
	 * 当前Invoker是否激活
	 * 
	 * @return
	 */
	public boolean actived();
}
