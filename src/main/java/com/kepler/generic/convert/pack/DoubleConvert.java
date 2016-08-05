package com.kepler.generic.convert.pack;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class DoubleConvert implements Convert {

	private static final String NAME = "Double";

	public Object convert(Object source, String extension) throws Exception {
		return Double.valueOf(source.toString());
	}

	public String name() {
		return DoubleConvert.NAME;
	}
}
