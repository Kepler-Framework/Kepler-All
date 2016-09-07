package com.kepler.header;

/**
 * @author kim 2015年7月14日
 */
public interface HeadersContext {

	public Headers get();

	public Headers set(Headers headers);

	/**
	 * 删除当前Headers
	 * 
	 * @return
	 */
	public Headers release();
}
