package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class DoubleConvertor implements Convertor {

	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Double d = Double.valueOf(source.toString());
		return Double.class.equals(expect) ? d : d.doubleValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Double.class.equals(clazz) || double.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
