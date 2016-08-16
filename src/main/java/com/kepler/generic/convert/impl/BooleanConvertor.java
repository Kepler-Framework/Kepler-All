package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class BooleanConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
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
