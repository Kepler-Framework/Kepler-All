package com.kepler.header;

/**
 * @author kim 2015年7月14日
 */
public interface HeadersContext {

	public Headers get();

	public Headers set(Headers headers);

	/**
	 * 返回并释放Headers
	 * 
	 * @return
	 */
	public Headers release();
}
