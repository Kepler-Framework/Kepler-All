package com.kepler.header.impl;

import java.util.UUID;

import org.springframework.util.StringUtils;

import com.kepler.header.Headers;
import com.kepler.trace.Trace;

/**
 * Trace上下文
 * 
 * @author KimShen
 *
 */
public class TraceContext {

	/**
	 * 获取上下文相关Trace, 如果不存在则创建. 如果未开启Header则返回Null
	 * 
	 * @return
	 */
	public static String trace() {
		if (Headers.ENABLED) {
			Headers headers = ThreadHeaders.HEADERS.get();
			String trace = headers.get(Trace.TRACE);
			return StringUtils.isEmpty(trace) ? TraceContext.generate(headers) : trace;
		} else {
			return null;
		}
	}

	/**
	 * 创建Trace
	 * 
	 * @param headers 当前上下文
	 * @return 创建后的Trace
	 */
	private static String generate(Headers headers) {
		// 使用UUID创建Trace
		String trace = UUID.randomUUID().toString();
		headers.put(Trace.TRACE, trace);
		return trace;
	}
}
