package com.kepler.protocol;

/**
 * @author kim
 *
 * 2016年3月16日
 */
public interface RequestProcessor {

	public Request process(Request request);

	/**
	 * 排序号
	 * 
	 * @return
	 */
	public int sort();
}
