package com.kepler.generic.reflect.convert.impl;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class ObjectConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		// 获取对象实际类型的Fields
		return analyser.get(extension.length != 0 ? extension[0] : expect).actual(source);
	}

	@Override
	public boolean support(Class<?> clazz) {
		// 不为内置(java)
		return !clazz.getName().startsWith("java");
	}

	public int sort() {
		return ConvertorPriority.LOW.priority();
	}
}
