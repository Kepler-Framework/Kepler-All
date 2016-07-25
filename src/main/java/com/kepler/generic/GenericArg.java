package com.kepler.generic;

import java.io.Serializable;

/**
 * 代理参数
 * 
 * @author KimShen
 *
 */
public interface GenericArg extends Serializable {

	/**
	 * 需要转换的参数类型
	 * 
	 * @return
	 * @throws Exception 转换失败
	 */
	public Class<?> clazz() throws Exception;
	
	/**
	 * 转换前的参数
	 * 
	 * @return
	 */
	public Object arg();
}
