package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class ShortConvert implements Convert {

	private static final String NAME = "short";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Short.valueOf(source.toString()).shortValue();
	}

	@Override
	public String name() {
		return ShortConvert.NAME;
	}
}
