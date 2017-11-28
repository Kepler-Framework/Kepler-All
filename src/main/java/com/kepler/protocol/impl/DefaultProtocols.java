package com.kepler.protocol.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.protocol.Protocols;

/**
 * @author KimShen
 *
 */
public class DefaultProtocols implements Protocols {

	private static final Log LOGGER = LogFactory.getLog(DefaultProtocols.class);

	private final Map<Byte, Class<?>> protocol;

	private final Class<?> def;

	public DefaultProtocols(Map<Byte, Class<?>> protocol, Class<?> def) {
		super();
		this.protocol = protocol;
		this.def = def;
	}

	@Override
	public Class<?> protocol(byte serial) {
		Class<?> protocol = this.protocol.get(serial);
		if (protocol == null) {
			DefaultProtocols.LOGGER.warn("[serial=" + serial + "][message=can not found]");
			return this.def;
		}
		return protocol;
	}
}
