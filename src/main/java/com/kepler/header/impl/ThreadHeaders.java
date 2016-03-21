package com.kepler.header.impl;

import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;

/**
 * 线程绑定Headers
 * 
 * @author kim 2015年7月14日
 */
public class ThreadHeaders implements HeadersContext {

	private static final ThreadLocal<Headers> HEADERS = new ThreadLocal<Headers>() {
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
		return ThreadHeaders.HEADERS.get().reset();
	}

	public Headers release() {
		// 仅Headers.enabled=true时触发Realease, 此时必须存在Headers
		Headers headers = ThreadHeaders.HEADERS.get();
		ThreadHeaders.HEADERS.set(null);
		ThreadHeaders.HEADERS.remove();
		return headers;
	}
}
