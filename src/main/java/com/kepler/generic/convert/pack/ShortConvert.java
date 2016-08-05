package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class ShortConvert implements Convert {

	private static final String NAME = "Short";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Short.valueOf(source.toString());
	}

	@Override
	public String name() {
		return ShortConvert.NAME;
	}
}
