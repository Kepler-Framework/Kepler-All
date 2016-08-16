package com.kepler.generic.analyse;

import java.lang.reflect.Method;

/**
 * Fields分析器
 * 
 * @author KimShen
 *
 */
public interface FieldsAnalyser {

	/**
	 * 获取Method相关的Feidls集合
	 * 
	 * @param method
	 * @return
	 */
	public Fields[] get(Method method);

	/**
	 * 获取Class相关的Fields
	 * 
	 * @param clazz
	 * @return
	 */
	public Fields get(Class<?> clazz);

	/**
	 * 获取Class和扩展相关的Fields
	 * 
	 * @param clazz
	 * @param extension
	 * @return
	 */
	public Fields get(Class<?> clazz, Class<?>[] extension);

	/**
	 * 分析Class相关的Fields
	 * 
	 * @param clazz
	 * @return
	 */
	public Fields set(Class<?> clazz);

	/**
	 * 分析Class相关的Fields
	 * 
	 * @param clazz
	 * @param extension 扩展信息
	 * @return
	 */
	public Fields set(Class<?> clazz, Class<?>[] extension);
}
