package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class ByteConvert implements Convert {

	private static final String NAME = "byte";

	public Object convert(Object source, String extension) throws Exception {
		return Byte.valueOf(source.toString()).byteValue();
	}

	public String name() {
		return ByteConvert.NAME;
	}
}
