package com.kepler.advised;

import java.lang.annotation.Annotation;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author KimShen
 *
 */
public class AdvisedFinder {

	public static Class<?> get(Object bean) throws BeansException {
		return AdvisedFinder.actual4class(bean);
	}

	/**
	 * 获取指定Bean中指定Annotation
	 * 
	 * @param bean
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static <T extends Annotation> T get(Object bean, Class<T> clazz) throws BeansException {
		return AnnotationUtils.findAnnotation(AdvisedFinder.actual4class(bean), clazz);
	}

	/**
	 * 递归, 直到获取实际类型
	 * 
	 * @param bean
	 * @return
	 * @throws Exception
	 */
	public static Class<?> actual4class(Object bean) throws BeansException {
		try {
			return Advised.class.isAssignableFrom(bean.getClass()) ? AdvisedFinder.actual4class(Advised.class.cast(bean).getTargetSource().getTarget()) : bean.getClass();
		} catch (Exception e) {
			throw new BeansUnvalidException(e);
		}
	}

	public static Object actual4object(Object bean) throws BeansException {
		try {
			return Advised.class.isAssignableFrom(bean.getClass()) ? AdvisedFinder.actual4class(Advised.class.cast(bean).getTargetSource().getTarget()) : bean;
		} catch (Exception e) {
			throw new BeansUnvalidException(e);
		}
	}

	private static class BeansUnvalidException extends BeansException {

		private static final long serialVersionUID = 1L;

		public BeansUnvalidException(Exception e) {
			super(e.getMessage());
		}
	}
}
