package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class BooleanConvert implements Convert {

	private static final String NAME = "Boolean";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Boolean.valueOf(source.toString());
	}

	@Override
	public String name() {
		return BooleanConvert.NAME;
	}

}
