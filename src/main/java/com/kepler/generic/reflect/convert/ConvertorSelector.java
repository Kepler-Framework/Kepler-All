package com.kepler.generic.reflect.convert;

/**
 * @author KimShen
 *
 */
public interface ConvertorSelector {

	/**
	 * 为指定Class选择Convertor
	 * 
	 * @param clazz 
	 * @return
	 * @throws Exception
	 */
	public Convertor select(Class<?> clazz);
}
