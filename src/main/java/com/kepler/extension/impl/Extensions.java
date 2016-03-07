package com.kepler.extension.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.kepler.KeplerValidateException;
import com.kepler.extension.Extension;

/**
 * @author kim 2015年7月14日
 */
public class Extensions implements BeanPostProcessor {

	private static final Log LOGGER = LogFactory.getLog(Extensions.class);

	private final List<Extension> extensions;

	public Extensions(List<Extension> extensions) {
		super();
		this.extensions = extensions;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	// Warning: 对于FactryBean构造的Bean如果没有被引用将无法被PostProcessAfterInitialization触发
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		for (Extension each : this.extensions) {
			// 轮询注册扩展点, 是否对需要对Bean进行安装(Extension不安装自身)
			if (each.interested().isAssignableFrom(Advised.class.isAssignableFrom(bean.getClass()) ? Advised.class.cast(bean).getTargetClass() : bean.getClass()) && !this.same(bean, each)) {
				each.install(bean);
				Extensions.LOGGER.debug(each + " installed " + bean + " ... ");
			}
		}
		return bean;
	}

	/**
	 * 扩展点禁止安装自身
	 * 
	 * @param bean
	 * @param each
	 * @return
	 */
	private boolean same(Object bean, Extension each) {
		try {
			Object source = Advised.class.isAssignableFrom(bean.getClass()) ? Advised.class.cast(bean).getTargetSource().getTarget() : bean;
			return source == each;
		} catch (Throwable throwable) {
			Extensions.LOGGER.error(throwable.getMessage(), throwable);
			throw new KeplerValidateException(throwable);
		}
	}
}
