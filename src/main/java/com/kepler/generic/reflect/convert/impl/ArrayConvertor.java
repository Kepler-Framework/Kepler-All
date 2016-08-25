package com.kepler.generic.reflect.convert.impl;

import java.lang.reflect.Array;

import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;
import com.kepler.generic.reflect.convert.Getter;

/**
 * @author KimShen
 *
 */
public class ArrayConvertor extends ComplexConvertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		// Guard case, 类型相等直接返回
		if (source.getClass().equals(expect)) {
			return source;
		}
		return this.convert(analyser, extension, super.getter(source));
	}

	private Object convert(FieldsAnalyser analyser, Class<?>[] extension, Getter source) throws Exception {
		Object actual = Array.newInstance(extension[0], source.length());
		// 获取数组实际类型对应Fields
		Fields fields = analyser.get(extension[0]);
		for (int index = 0; index < source.length(); index++) {
			Array.set(actual, index, fields.actual(source.next()));
		}
		return actual;
	}

	@Override
	public boolean support(Class<?> clazz) {
		return clazz.isArray();
	}

	public int sort() {
		return ConvertorPriority.DEFAULT.priority();
	}
}
