package com.kepler.generic.reflect.convert.impl;

import java.util.Collection;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;

public abstract class SimpleConvertor implements Convertor {

	@SuppressWarnings("rawtypes")
	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser)
			throws Exception {
		Class<?> sourceKlass = source.getClass();
		if (sourceKlass.isArray()) {
			source = ((Object[])source)[0];
		} else if (Collection.class.isAssignableFrom(sourceKlass)) {
			source = ((Collection)source).toArray()[0];
		}
		if (expect.isAssignableFrom(source.getClass())) {
			return source;
		}
		return doConvert(source, expect, extension, analyser);
	}

	protected abstract Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception;

}
