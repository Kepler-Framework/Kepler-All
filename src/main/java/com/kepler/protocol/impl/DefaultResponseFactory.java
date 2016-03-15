package com.kepler.protocol.impl;

import com.kepler.protocol.Bytes;
import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactory;

/**
 * @author kim 2015年7月8日
 */
public class DefaultResponseFactory implements ResponseFactory {

	@Override
	public Response response(Bytes ack, Object response, byte serial) {
		return new DefaultResponse(serial, ack, response);
	}

	public Response throwable(Bytes ack, Throwable throwable, byte serial) {
		return new DefaultResponse(serial, ack, throwable);
	}
}
