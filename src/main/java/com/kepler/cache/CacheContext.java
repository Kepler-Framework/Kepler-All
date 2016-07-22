package com.kepler.cache;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface CacheContext {

	/**
	 * 获取缓存
	 * 
	 * @param service
	 * @param method
	 * @return 当前缓存对象, 通过expired判断是否过期
	 */
	public Cache get(Service service, String method);

	/**
	 * 更新缓存
	 * 
	 * @param service
	 * @param method
	 * @param response 需要更新的缓存值
	 */
	public void set(Service service, String method, Object response);
}
