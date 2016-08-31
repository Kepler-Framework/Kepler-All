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
	 * 获取上下文相关Trace, 如果不存在则创建. 如果未开启Header则返回Null
	 * 
	 * @return
	 */
	public static String trace() {
		// 开启Trace并开启Header
		if (Headers.ENABLED && Trace.ENABLED_DEF) {
			Headers headers = ThreadHeaders.HEADERS.get();
			String trace = headers.get(Trace.TRACE);
			// 如果Trace不存在则创建Trace
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
		headers.put(Trace.TRACE, TraceContext.log4jmdc(trace));
		return trace;
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
