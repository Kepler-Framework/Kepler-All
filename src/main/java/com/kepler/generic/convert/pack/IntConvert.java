package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class IntConvert implements Convert {

	private static final String NAME = "Int";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Integer.valueOf(source.toString());
	}

	@Override
	public String name() {
		return IntConvert.NAME;
	}
}
