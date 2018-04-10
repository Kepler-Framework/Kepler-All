package com.kepler.connection.impl;

import com.kepler.KeplerLocalException;

import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author kim 2015年7月8日
 */
public class DefaultChannelFactory<T extends Channel> implements ChannelFactory<T> {

	public static final ChannelFactory<ServerChannel> INSTANCE_SERVER = new DefaultChannelFactory<ServerChannel>(NioServerSocketChannel.class);

	public static final ChannelFactory<SocketChannel> INSTANCE_CLIENT = new DefaultChannelFactory<SocketChannel>(NioSocketChannel.class);
	
	private final Class<? extends T> factory;

	public DefaultChannelFactory(Class<? extends T> factory) {
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
