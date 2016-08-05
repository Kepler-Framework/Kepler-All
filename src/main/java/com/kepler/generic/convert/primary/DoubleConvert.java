package com.kepler.generic.convert.primary;

import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class DoubleConvert implements Convert {

	private static final String NAME = "double";

	public Object convert(Object source, String extension) throws Exception {
		return Double.valueOf(source.toString()).doubleValue();
	}

	public String name() {
		return DoubleConvert.NAME;
	}
}
