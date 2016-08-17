package com.kepler.generic.reflect.convert.impl;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class LongConvertor implements Convertor {

	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Long l = Long.valueOf(source.toString());
		return Long.class.equals(expect) ? l : l.longValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Long.class.equals(clazz) || long.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
