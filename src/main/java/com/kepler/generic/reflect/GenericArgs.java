package com.kepler.generic.reflect;

import java.io.Serializable;

/**
 * 泛化参数包装
 * 
 * @author KimShen
 *
 */
public interface GenericArgs extends Serializable {

	/**
	 * 获取实际Class
	 * 
	 * @return
	 * @throws Exception
	 */
	public Class<?>[] classes() throws Exception;
	
	/**
	 * 获取实际参数
	 * 
	 * @return
	 */
	public Object[] args();
	
	/**
	 * 是否需要猜测参数
	 * 
	 * @return
	 */
	public boolean guess();

}
