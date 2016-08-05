package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class LongConvert implements Convert {

	private static final String NAME = "long";

	public Object convert(Object source, String extension) throws Exception {
		return Long.valueOf(source.toString()).longValue();
	}

	public String name() {
		return LongConvert.NAME;
	}
}
