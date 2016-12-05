package com.kepler.generic.reflect.convert.impl;

import java.util.Collection;

import com.kepler.KeplerValidateException;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;

/**
 * @author zhangjiehao
 *
 */
public abstract class SimpleConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		// Guard case, 类型兼容直接返回
		if (expect.isAssignableFrom(source.getClass())) {
			return source;
		}
		// 转换参数
		Object source_convert = this.convertSource(source, expect);
		return this.doConvert(source_convert, expect, extension, analyser);
	}

	/**
	 * 从集合提取参数
	 * 
	 * @param source 请求参数
	 * @param expect 预期类型
	 * @return
	 */
	private Object convertSource(Object source, Class<?> expect) {
		// Guard case1: Array
		if (source.getClass().isArray()) {
			return Object[].class.cast(source)[0];
		}
		// Guard case2: Collection
		if (Collection.class.isAssignableFrom(source.getClass())) {
			Collection<?> source_collection = Collection.class.cast(source);
			if (source_collection.isEmpty()) {
				throw new KeplerValidateException("Invalid data for arg: " + expect);
			} else {
				return source_collection.iterator().next();
			}
		}
		return source;
	}

	protected abstract Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception;
}
