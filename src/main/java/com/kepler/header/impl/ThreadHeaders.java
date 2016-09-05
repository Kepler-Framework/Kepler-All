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

	public Headers reset() {
		// 如果从服务端传递Header并且未开启则可能为Null
		Headers headers = ThreadHeaders.HEADERS.get();
		if (headers != null) {
			headers.reset();
		}
		return headers;
	}
}
