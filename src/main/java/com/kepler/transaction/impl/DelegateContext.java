package com.kepler.transaction.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.kepler.KeplerTranscationException;
import com.kepler.annotation.Rollback;
import com.kepler.transaction.Guid;
import com.kepler.transaction.Invoker;
import com.kepler.transaction.Transcation;

/**
 * 事务代理
 * 
 * @author KimShen
 *
 */
public class DelegateContext {

	private final Transcation transcation;

	public DelegateContext(Transcation transcation) {
		super();
		this.transcation = transcation;
	}

	public Object process(ProceedingJoinPoint point) throws Throwable {
		// GUID == null ? Invoke : Rollback
		return StringUtils.isEmpty(Guid.get()) ? this.invoke(point) : point.proceed(point.getArgs());
	}

	private Object invoke(ProceedingJoinPoint point) throws Throwable {
		// 构造回滚数据
		Rollback rollback = AnnotationUtils.getAnnotation(MethodSignature.class.cast(point.getSignature()).getMethod(), Rollback.class);
		DefaultRequest request = new DefaultRequest(new DefaultLocation(rollback.clazz(), rollback.method()), point.getArgs());
		return this.transcation.commit(request, new DefaultInvoker(point));
	}

	private class DefaultInvoker implements Invoker {

		private final ProceedingJoinPoint point;

		private DefaultInvoker(ProceedingJoinPoint point) {
			super();
			this.point = point;
		}

		@Override
		public Object invoke(String uuid, Object... args) throws Exception {
			try {
				return this.point.proceed(this.point.getArgs());
			} catch (Throwable e) {
				throw Exception.class.isAssignableFrom(e.getClass()) ? Exception.class.cast(e) : new KeplerTranscationException(e);
			}
		}
	}
}
