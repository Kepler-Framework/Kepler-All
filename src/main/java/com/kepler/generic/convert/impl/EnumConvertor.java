package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class EnumConvertor implements Convertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
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
