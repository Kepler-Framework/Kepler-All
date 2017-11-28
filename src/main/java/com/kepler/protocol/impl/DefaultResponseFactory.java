package com.kepler.protocol.impl;

import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactory;

/**
 * @author kim 2015年7月8日
 */
abstract public class DefaultResponseFactory implements ResponseFactory {

	@Override
	public Response response(byte[] ack, Object response, byte serial) {
		return new DefaultResponse(serial, ack, response);
	}

	public Response throwable(byte[] ack, Throwable throwable, byte serial) {
		return new DefaultResponse(serial, ack, throwable);
	}
}
