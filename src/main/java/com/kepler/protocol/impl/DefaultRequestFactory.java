package com.kepler.protocol.impl;

import java.lang.reflect.Method;

import com.kepler.header.Headers;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class DefaultRequestFactory implements RequestFactory {

	public Request request(Headers headers, Service service, String method, boolean async, Object[] args, Class<?>[] types, Integer ack, byte serial) {
		return new DefaultRequest(ack, headers, service, method, async, args, types, serial);
	}

	public Request request(Headers headers, Service service, Method method, boolean async, Object[] args, Integer ack, byte serial) {
		return new DefaultRequest(ack, headers, service, method, async, args, serial);
	}

	public Request request(Request request, Integer ack, Object[] args) {
		return new DefaultRequest(request, ack, args);
	}

	public Request request(Request request, Integer ack, boolean async) {
		return new DefaultRequest(request, ack, async);
	}

	public Request request(Request request, Integer ack) {
		return new DefaultRequest(request, ack);
	}
}
