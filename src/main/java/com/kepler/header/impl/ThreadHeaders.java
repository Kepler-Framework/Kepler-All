package com.kepler.header.impl;

import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;

/**
 * 线程绑定Headers
 * 
 * @author kim 2015年7月14日
 */
public class ThreadHeaders implements HeadersContext {

	static final ThreadLocal<Headers> HEADERS = new ThreadLocal<Headers>() {
		protected Headers initialValue() {
			// Create on get() or release()
			return new LazyHeaders();
		}
	};

	@Override
	public Headers get() {
		return ThreadHeaders.HEADERS.get();
	}

	@Override
	public Headers set(Headers headers) {
		ThreadHeaders.HEADERS.set(headers);
		return headers;
	}

	public Headers release() {
		Headers headers = ThreadHeaders.HEADERS.get();
		ThreadHeaders.HEADERS.remove();
		return headers;
	}
}
