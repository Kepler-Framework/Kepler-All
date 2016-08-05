package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class IntConvert implements Convert {

	private static final String NAME = "int";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return Integer.valueOf(source.toString()).intValue();
	}

	@Override
	public String name() {
		return IntConvert.NAME;
	}
}
