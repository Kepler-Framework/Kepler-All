package com.kepler.generic.reflect.convert.impl;

import java.math.BigDecimal;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class BigDecimalConvertor extends SimpleConvertor {

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		return new BigDecimal(source.toString());
	}

	@Override
	public boolean support(Class<?> clazz) {
		return BigDecimal.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
