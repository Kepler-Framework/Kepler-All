package com.kepler.connection;

import java.net.SocketAddress;

import com.kepler.KeplerValidateException;
import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年3月9日
 */
public interface Reject {

	public Request reject(Request request, SocketAddress address) throws KeplerValidateException;

	public String name();
}
