package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class ByteConvert implements Convert {

	private static final String NAME = "Byte";

	public Object convert(Object source, String extension) throws Exception {
		return Byte.valueOf(source.toString());
	}

	public String name() {
		return ByteConvert.NAME;
	}
}
