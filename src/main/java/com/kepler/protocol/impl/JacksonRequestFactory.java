package com.kepler.protocol.impl;

import java.lang.reflect.Method;

import com.kepler.header.Headers;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.serial.jackson.JacksonSerial;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class JacksonRequestFactory implements RequestFactory {

	public Request request(Headers headers, Service service, String method, boolean async, Object[] args, Class<?>[] types, byte[] ack, byte serial) {
		return new JacksonRequest(ack, headers, service, method, async, args, types, serial);
	}

	public Request request(Headers headers, Service service, Method method, boolean async, Object[] args, byte[] ack, byte serial) {
		return new JacksonRequest(ack, headers, service, method, async, args, serial);
	}

	public Request request(Request request, byte[] ack, Object[] args) {
		return new JacksonRequest(request, ack, args);
	}

	public Request request(Request request, byte[] ack, boolean async) {
		return new JacksonRequest(request, ack, async);
	}

	public Request request(Request request, byte[] ack) {
		return new JacksonRequest(request, ack);
	}

	@Override
	public byte serial() {
		return JacksonSerial.SERIAL;
	}
}
