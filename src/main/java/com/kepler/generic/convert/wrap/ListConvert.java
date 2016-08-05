package com.kepler.generic.convert.wrap;

import com.kepler.generic.GenericArgs;
import com.kepler.generic.arg.ListArg;

/**
 * @author KimShen
 *
 */
public class ListConvert extends ArgsConvert {

	private static final String NAME = "list";

	@Override
	protected GenericArgs args(String extension) {
		return new ListArg();
	}

	@Override
	public String name() {
		return ListConvert.NAME;
	}

}
