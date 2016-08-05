package com.kepler.generic.convert.wrap;

import com.kepler.generic.GenericArgs;
import com.kepler.generic.arg.SetArg;

/**
 * @author KimShen
 *
 */
public class SetConvert extends ArgsConvert {

	private static final String NAME = "set";

	@Override
	protected GenericArgs args(String extension) {
		return new SetArg();
	}

	@Override
	public String name() {
		return SetConvert.NAME;
	}

}
