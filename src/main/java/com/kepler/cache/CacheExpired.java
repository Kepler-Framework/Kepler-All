package com.kepler.cache;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface CacheExpired {

	/**
	 * 失效指定缓存
	 * 
	 * @param service
	 * @param method
	 * @return
	 */
	public boolean expired(Service service, String method);
}
