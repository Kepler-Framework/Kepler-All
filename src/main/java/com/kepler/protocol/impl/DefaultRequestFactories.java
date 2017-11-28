package com.kepler.protocol.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.protocol.RequestFactories;
import com.kepler.protocol.RequestFactory;

/**
 * @author KimShen
 *
 */
public class DefaultRequestFactories implements RequestFactories {

	private static final Log LOGGER = LogFactory.getLog(DefaultRequestFactories.class);

	private final Map<Byte, RequestFactory> factory = new HashMap<Byte, RequestFactory>();

	private final RequestFactory def;

	public DefaultRequestFactories(List<RequestFactory> factory, RequestFactory def) {
		super();
		this.def = def;
		for (RequestFactory each : factory) {
			this.factory.put(each.serial(), each);
		}
	}

	@Override
	public RequestFactory factory(byte serial) {
		RequestFactory factory = this.factory.get(serial);
		if (factory == null) {
			DefaultRequestFactories.LOGGER.warn("[serial=" + serial + "][message=can not found]");
			return this.def;
		}
		return factory;
	}
}
