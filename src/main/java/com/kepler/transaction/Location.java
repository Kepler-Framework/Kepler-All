package com.kepler.transaction;

import java.io.Serializable;

/**
 * 回滚位置
 * 
 * @author KimShen
 *
 */
public interface Location extends Serializable {

	/**
	 * 指定方法
	 * 
	 * @return
	 */
	public String method();

	/**
	 * 指定处理类
	 * 
	 * @return
	 */
	public Class<?> clazz();
}
