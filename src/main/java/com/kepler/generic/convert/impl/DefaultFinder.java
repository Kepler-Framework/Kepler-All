package com.kepler.generic.convert.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerGenericException;
import com.kepler.extension.Extension;
import com.kepler.generic.convert.Convert;
import com.kepler.generic.convert.ConvertFinder;

/**
 * @author KimShen
 *
 */
public class DefaultFinder implements Extension, ConvertFinder {

	/**
	 * Target - Convert
	 */
	private final Map<String, Convert> converts = new HashMap<String, Convert>();

	@Override
	public Convert find(String target) throws Exception {
		Convert convert = this.converts.get(target);
		if (convert == null) {
			throw new KeplerGenericException("Convert: " + target + " can not be found");
		}
		return convert;
	}

	@Override
	public DefaultFinder install(Object instance) {
		Convert convert = Convert.class.cast(instance);
		this.converts.put(convert.name(), convert);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Convert.class;
	}
}
