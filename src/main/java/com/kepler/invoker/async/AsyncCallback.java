package com.kepler.invoker.async;

/**
 * @author kim
 *
 * 2016年2月16日
 */
abstract public class AsyncCallback {


	/**
	 * 成功时回调
	 * 
	 * @param args
	 */
	abstract protected void callback(Object... args);

	/**
	 * 失败时回调
	 * 
	 * @param throwable
	 */
	abstract protected void throwable(Throwable throwable);
}
