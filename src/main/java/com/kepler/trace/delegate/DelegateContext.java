package com.kepler.trace.delegate;

import org.aspectj.lang.ProceedingJoinPoint;

import com.kepler.header.impl.TraceContext;
import com.kepler.id.impl.GuidGenerator;

/**
 * @author KimShen
 *
 */
public class DelegateContext {

	private final GuidGenerator generator;

	public DelegateContext(GuidGenerator generator) {
		super();
		this.generator = generator;
	}

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
			TraceContext.getTraceOnCreate(this.generator.toString(this.generator.generate()));
			return point.proceed(point.getArgs());
		} finally {
			// 释放Trace
			TraceContext.release();
		}
	}
}
