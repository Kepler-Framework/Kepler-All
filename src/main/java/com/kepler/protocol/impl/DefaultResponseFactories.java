package com.kepler.protocol.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.protocol.ResponseFactories;
import com.kepler.protocol.ResponseFactory;

/**
 * @author KimShen
 *
 */
public class DefaultResponseFactories implements ResponseFactories {

	private static final Log LOGGER = LogFactory.getLog(DefaultResponseFactories.class);

	private final Map<Byte, ResponseFactory> factory = new HashMap<Byte, ResponseFactory>();

	private final ResponseFactory def;

	public DefaultResponseFactories(List<ResponseFactory> factory, ResponseFactory def) {
		super();
		this.def = def;
		for (ResponseFactory each : factory) {
			this.factory.put(each.serial(), each);
		}
	}

	@Override
	public ResponseFactory factory(byte serial) {
		ResponseFactory factory = this.factory.get(serial);
		if (factory == null) {
			DefaultResponseFactories.LOGGER.warn("[serial=" + serial + "][message=can not found]");
			return this.def;
		}
		return factory;
	}
}
