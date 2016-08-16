package com.kepler.generic.analyse;

/**
 * 属性集合
 * 
 * @author KimShen
 *
 */
public interface Fields {

	/**
	 * 获取真实参数
	 * 
	 * @param source 原始参数
	 * @return 真实参数
	 * @throws Exception
	 */
	public Object actual(Object source) throws Exception;
}
