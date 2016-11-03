package com.kepler.header.impl;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.trace.Trace;

/**
 * Trace上下文
 * 
 * @author KimShen
 *
 */
public class TraceContext {

	/**
	 * 是否开启Log4j.MDC
	 */
	private static final boolean MDC = PropertiesUtils.get(TraceContext.class.getName().toLowerCase() + ".log4j_mdc", false);

	private static final Log LOGGER = LogFactory.getLog(TraceContext.class);

	/**
	 * Log4j.MDC
	 */
	private static Class<?> CLASS;

	static {
		// 加载Log4j.MDC
		if (TraceContext.MDC) {
			try {
				TraceContext.CLASS = Class.forName("org.apache.log4j.MDC");
			} catch (Exception e) {
				TraceContext.LOGGER.warn(e.getMessage(), e);
			}
		}
	}

	/**
	 * 尝试从上下文获取Headers
	 * 
	 * @return
	 */
	private static Headers headers() {
		// 如果作为服务并且上游系统未开启Header则Header可能为Null
		Headers headers = ThreadHeaders.HEADERS.get();
		if (headers == null) {
			ThreadHeaders.HEADERS.set(headers = new LazyHeaders());
		}
		return headers;
	}

	/**
	 * 创建Trace
	 * 
	 * @param headers 当前上下文
	 * @return 创建后的Trace
	 */
	private static String trace(Headers headers, String trace) {
		// 生成Trace
		trace = StringUtils.isEmpty(trace) ? UUID.randomUUID().toString() : trace;
		headers.put(Trace.TRACE_COVER, TraceContext.log4jmdc(trace));
		return trace;
	}

	/**
	 * 获取上下文相关Trace, 如果不存在则返回Null.
	 * 
	 * @return
	 */
	public static String get() {
		Headers headers = TraceContext.headers();
		return headers.get(Trace.TRACE_COVER);
	}

	/**
	 * 获取上下文相关Trace, 如果不存在则创建.
	 * 
	 * @return
	 */
	public static String get4create() {
		return TraceContext.get4create(null);
	}

	public static String get4create(String trace) {
		Headers headers = TraceContext.headers();
		String current = headers.get(Trace.TRACE_COVER);
		// 如果当前不存在Trace则创建
		return StringUtils.isEmpty(current) ? TraceContext.trace(headers, trace) : current;
	}

	/**
	 * 释放上下文相关Trace(将导致之后的调用Trace中断)
	 * 
	 * @return
	 */
	public static void release() {
		// 如果从服务端传递的Header并且未开启则可能为Null
		Headers headers = ThreadHeaders.HEADERS.get();
		if (headers != null) {
			headers.delete(Trace.TRACE_COVER);
		}
	}

	/**
	 * 开启MDC
	 * 
	 * @param trace
	 */
	private static String log4jmdc(String trace) {
		try {
			if (TraceContext.CLASS != null) {
				MethodUtils.invokeStaticMethod(TraceContext.CLASS, "put", new Object[] { Trace.TRACE, trace });
			}
		} catch (Exception e) {
			TraceContext.LOGGER.error(e.getMessage(), e);
		}
		return trace;
	}
}
