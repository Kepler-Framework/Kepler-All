package com.kepler.generic.convert.wrap;

import com.kepler.generic.arg.EnumArg;
import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class EnumConvert implements Convert {

	private static final String NAME = "enum";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		return new EnumArg(extension, source.toString());
	}

	@Override
	public String name() {
		return EnumConvert.NAME;
	}
}
