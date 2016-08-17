package com.kepler.generic.wrap.arg;

import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerGenericException;
import com.kepler.generic.wrap.GenericArg;

/**
 * Map转换
 * 
 * @author KimShen
 *
 */
public class MapArg extends HashMap<Object, Object> implements GenericArg {

	private static final long serialVersionUID = 1L;

	public MapArg() {
		super();
	}

	public MapArg(Map<Object, Object> args) {
		super();
		super.putAll(args);
	}

	public MapArg put(Object key, Object value) {
		super.put(key, value);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		Map<Object, Object> expect = new HashMap<Object, Object>(super.size());
		for (Object key : super.keySet()) {
			Object value = super.get(key);
			// 转换
			expect.put(GenericArg.class.isAssignableFrom(key.getClass()) ? GenericArg.class.cast(key).arg() : key, GenericArg.class.isAssignableFrom(value.getClass()) ? GenericArg.class.cast(value).arg() : value);
		}
		return expect;
	}
}
