package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class BooleanConvert implements Convert {

	private static final String NAME = "boolean";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Boolean.valueOf(source.toString()).booleanValue();
	}

	@Override
	public String name() {
		return BooleanConvert.NAME;
	}

}
