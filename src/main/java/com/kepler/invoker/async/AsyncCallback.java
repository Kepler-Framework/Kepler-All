package com.kepler.invoker.async;

/**
 * @author kim
 *
 * 2016年2月16日
 */
abstract public class AsyncCallback implements Runnable {

	/**
	 * 回调参数值
	 */
	private Object[] args;

	/**
	 * 准备参数
	 * 
	 * @param args
	 * @return
	 */
	AsyncCallback prepare(Object... args) {
		this.args = args;
		return this;
	}

	public void run() {
		this.callback(this.args);
	}

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
