package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class FloatConvertor implements Convertor {

	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Float f = Float.valueOf(source.toString());
		return Float.class.equals(expect) ? f : f.floatValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Float.class.equals(clazz) || float.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
