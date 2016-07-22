package com.kepler.cache;

/**
 * 相对于Servce.method的缓存
 * 
 * @author KimShen
 *
 */
public interface Cache {

	/**
	 * 获取缓存
	 * 
	 * @return
	 */
	public Object get();

	/**
	 * 重置缓存
	 * 
	 * @param response 缓存内容
	 * @return
	 */
	public Object set(Object response);

	/**
	 * 是否过期
	 * 
	 * @return
	 */
	public boolean expired();
}
