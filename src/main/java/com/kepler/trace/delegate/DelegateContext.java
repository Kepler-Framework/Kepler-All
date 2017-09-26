package com.kepler.trace.delegate;

import org.aspectj.lang.ProceedingJoinPoint;

import com.kepler.header.impl.TraceContext;

/**
 * @author KimShen
 *
 */
public class DelegateContext {

	/**
	 * 为上下文线程添加Trace
	 * 
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	public Object process(ProceedingJoinPoint point) throws Throwable {
		try {
			// 加载Trace
			TraceContext.getTraceOnCreate();
			return point.proceed(point.getArgs());
		} finally {
			// 释放Trace
			TraceContext.release();
		}
	}
}
