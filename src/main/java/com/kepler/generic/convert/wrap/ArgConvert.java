package com.kepler.generic.convert.wrap;

import java.util.Map;

import com.kepler.generic.arg.ObjectArg;
import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
public class ArgConvert implements Convert {

	private static final String NAME = "object";

	@SuppressWarnings("unchecked")
	@Override
	public Object convert(Object source, String extension) throws Exception {
		//必须为Map<String, Object>对象
		return new ObjectArg(extension, Map.class.cast(source));
	}

	@Override
	public String name() {
		return ArgConvert.NAME;
	}
}
