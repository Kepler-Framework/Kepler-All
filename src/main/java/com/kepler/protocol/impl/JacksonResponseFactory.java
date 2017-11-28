package com.kepler.protocol.impl;

import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactory;
import com.kepler.serial.jackson.JacksonSerial;

/**
 * @author KimShen
 *
 */
public class JacksonResponseFactory implements ResponseFactory {

	@Override
	public Response response(byte[] ack, Object response, byte serial) {
		return new JacksonResponse(serial, ack, response);
	}

	public Response throwable(byte[] ack, Throwable throwable, byte serial) {
		return new JacksonResponse(serial, ack, throwable);
	}

	@Override
	public byte serial() {
		return JacksonSerial.SERIAL;
	}
}
