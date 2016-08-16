package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class IntConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Integer i = Integer.valueOf(source.toString());
		return Integer.class.equals(expect) ? i : i.intValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Integer.class.equals(clazz) || int.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
