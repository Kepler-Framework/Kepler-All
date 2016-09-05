package com.kepler.generic.reflect.convert.impl;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class ShortConvertor extends SimpleConvertor {

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Short s = Short.valueOf(source.toString());
		return Short.class.equals(expect) ? s : s.shortValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Short.class.equals(clazz) || short.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
