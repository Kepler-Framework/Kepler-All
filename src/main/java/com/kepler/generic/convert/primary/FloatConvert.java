package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class FloatConvert implements Convert {

	private static final String NAME = "float";

	public Object convert(Object source, String extension) throws Exception {
		return Float.valueOf(source.toString()).floatValue();
	}

	public String name() {
		return FloatConvert.NAME;
	}
}
