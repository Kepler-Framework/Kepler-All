package com.kepler.connection.impl;

import com.kepler.KeplerLocalException;

import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;

/**
 * @author kim 2015年7月8日
 */
class DefaultChannelFactory<T extends Channel> implements ChannelFactory<T> {

	private final Class<? extends T> factory;

	DefaultChannelFactory(Class<? extends T> factory) {
		super();
		this.factory = factory;
	}

	public T newChannel() {
		try {
			return this.factory.newInstance();
		} catch (Exception e) {
			throw new KeplerLocalException(e);
		}
	}
}
