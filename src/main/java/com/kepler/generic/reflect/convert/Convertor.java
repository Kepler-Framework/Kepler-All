package com.kepler.generic.reflect.convert;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;

/**
 * 转换器
 * 
 * @author KimShen
 *
 */
public interface Convertor {

	/**
	 * @param source 原始参数
	 * @param expect 预期类型
	 * @param extension 扩展信息
	 * @param analyser 分析器,用于回调
	 * @return 真实参数
	 * @throws Exception
	 */
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception;

	/**
	 * 是否支持该类型转换
	 * 
	 * @param clazz
	 * @return
	 */
	public boolean support(Class<?> clazz);

	/**
	 * 优先级
	 * 
	 * @return
	 */
	public int sort();
}
