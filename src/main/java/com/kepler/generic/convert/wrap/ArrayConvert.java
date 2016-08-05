package com.kepler.generic.convert.wrap;

import com.kepler.generic.GenericArgs;
import com.kepler.generic.arg.ArrayArg;

/**
 * @author KimShen
 *
 */
public class ArrayConvert extends ArgsConvert {

	private static final String NAME = "array";

	@Override
	protected GenericArgs args(String extension) {
		return new ArrayArg(extension);
	}

	@Override
	public String name() {
		return ArrayConvert.NAME;
	}
}
