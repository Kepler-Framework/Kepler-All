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

	/**
	 * 如果请求被拒绝则抛出KeplerValidateException
	 * 
	 * @param request
	 * @param address
	 * @return
	 * @throws KeplerValidateException
	 */
	public Request reject(Request request, SocketAddress address) throws KeplerValidateException;

	public String name();
}
