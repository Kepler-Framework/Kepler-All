package com.kepler.generic.reflect.convert.impl;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class EnumConvertor extends SimpleConvertor {

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		return MethodUtils.invokeStaticMethod(expect, "valueOf", source.toString());
	}

	@Override
	public boolean support(Class<?> clazz) {
		return clazz.isEnum();
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
