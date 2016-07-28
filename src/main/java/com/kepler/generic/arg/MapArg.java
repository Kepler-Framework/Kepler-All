package com.kepler.generic.arg;

import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArg;

/**
 * Map转换
 * 
 * @author KimShen
 *
 */
public class MapArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	private Map<Object, Object> args;

	public MapArg() {
		super();
		this.args = new HashMap<Object, Object>();
	}

	public MapArg(Map<Object, Object> args) {
		super();
		this.args = args;
	}

	public MapArg put(Object key, Object value) {
		this.args.put(key, value);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		Map<Object, Object> expect = new HashMap<Object, Object>(this.args.size());
		for (Object key : this.args.keySet()) {
			Object value = this.args.get(key);
			// 转换
			expect.put(GenericArg.class.isAssignableFrom(key.getClass()) ? GenericArg.class.cast(key).arg() : key, GenericArg.class.isAssignableFrom(value.getClass()) ? GenericArg.class.cast(value).arg() : value);
		}
		return expect;
	}
}
