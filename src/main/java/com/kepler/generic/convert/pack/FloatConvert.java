package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class FloatConvert implements Convert {

	private static final String NAME = "Float";

	public Object convert(Object source, String extension) throws Exception {
		return Float.valueOf(source.toString());
	}

	public String name() {
		return FloatConvert.NAME;
	}
}
