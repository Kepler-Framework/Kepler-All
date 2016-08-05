package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class LongConvert implements Convert {

	private static final String NAME = "Long";

	public Object convert(Object source, String extension) throws Exception {
		return Long.valueOf(source.toString());
	}

	public String name() {
		return LongConvert.NAME;
	}
}
