package com.kepler.trace;

import java.util.List;

import com.kepler.protocol.Request;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface TraceCauses {

	/**
	 * @param request 
	 */
	public void put(Request request, Throwable throwable);

	public void put(Service service, String method, String cause);

	/**
	 * 获取并重置
	 * 
	 * @return
	 */
	public List<TraceCause> get();
}
