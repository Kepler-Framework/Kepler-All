package com.kepler.generic.analyse;

/**
 * 属性集合过滤器
 * 
 * @author KimShen
 *
 */
public interface FieldsFilter {

	/**
	 * 指定Class是否符合属性集合条件
	 * 
	 * @param clazz
	 * @return 
	 */
	public boolean filter(Class<?> clazz);
}
