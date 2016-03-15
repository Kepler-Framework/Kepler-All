package com.kepler.protocol.impl;

import java.lang.reflect.Method;

import com.kepler.header.Headers;
import com.kepler.protocol.Bytes;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class DefaultRequestFactory implements RequestFactory {

	public Request request(Headers headers, Service service, String method, boolean async, Object[] args, Class<?>[] types, Bytes ack, byte serial) {
		return new DefaultRequest(ack, headers, service, method, async, args, types, serial);
	}

	public Request request(Headers headers, Service service, Method method, boolean async, Object[] args, Bytes ack, byte serial) {
		return new DefaultRequest(ack, headers, service, method, async, args, serial);
	}

	public Request request(Request request, Bytes ack, Object[] args) {
		return new DefaultRequest(request, ack, args);
	}

	public Request request(Request request, Bytes ack, boolean async) {
		return new DefaultRequest(request, ack, async);
	}

	public Request request(Request request, Bytes ack) {
		return new DefaultRequest(request, ack);
	}
}
