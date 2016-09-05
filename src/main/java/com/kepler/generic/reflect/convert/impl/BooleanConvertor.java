package com.kepler.generic.reflect.convert.impl;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class BooleanConvertor extends SimpleConvertor {

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Boolean b = Boolean.valueOf(source.toString());
		return Boolean.class.equals(expect) ? b : b.booleanValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Boolean.class.equals(clazz) || boolean.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
