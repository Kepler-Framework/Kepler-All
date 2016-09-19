package com.kepler.generic.reflect.convert.impl;

import java.math.BigInteger;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class BigIntegerConvertor extends SimpleConvertor {

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		return new BigInteger(source.toString());
	}

	@Override
	public boolean support(Class<?> clazz) {
		return BigInteger.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
