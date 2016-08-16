package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class ShortConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
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
