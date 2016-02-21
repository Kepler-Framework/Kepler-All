package com.kepler.header;

import com.kepler.service.Service;

/**
 * @author kim 2015年7月14日
 */
public interface HeadersProcessor {

	public Headers process(Service service, Headers headers);

	/**
	 * 排序号
	 * 
	 * @return
	 */
	public int sort();
}
