package com.kepler.generic.convert.impl;

import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class ByteConvertor implements Convertor {

	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Byte b = Byte.valueOf(source.toString());
		return Byte.class.equals(expect) ? b : b.byteValue();
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Byte.class.equals(clazz) || byte.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
