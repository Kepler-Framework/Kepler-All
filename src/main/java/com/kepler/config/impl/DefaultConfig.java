package com.kepler.config.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.kepler.annotation.Async;
import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;
import com.kepler.config.Config;
import com.kepler.config.ConfigAware;
import com.kepler.config.ConfigParser;
import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author kim 2015年12月27日
 */
public class DefaultConfig implements Config, BeanPostProcessor {

	/**
	 * 是否所有Spring托管Bean均允许回调
	 */
	private static final boolean ALL = PropertiesUtils.get(DefaultConfig.class.getName().toLowerCase() + ".all", true);

	private static final Log LOGGER = LogFactory.getLog(DefaultConfig.class);

	/**
	 * 基础/包装类映射(byte -> Byte)
	 */
	private final Map<Class<?>, Class<?>> mapping = new HashMap<Class<?>, Class<?>>();

	private final Invokers invokers = new Invokers();

	private final ThreadPoolExecutor threads;

	private final ConfigParser parser;

	private final ConfigAware aware;

	public DefaultConfig(ThreadPoolExecutor threads, ConfigAware aware, ConfigParser parser) {
		super();
		this.threads = threads;
		this.parser = parser;
		this.aware = aware;
	}

	/**
	 * 加载映射
	 */
	public void init() {
		this.mapping.put(boolean.class, Boolean.class);
		this.mapping.put(short.class, Short.class);
		this.mapping.put(int.class, Integer.class);
		this.mapping.put(long.class, Long.class);
		this.mapping.put(byte.class, Byte.class);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return this.binding(bean, Advised.class.isAssignableFrom(bean.getClass()) ? Advised.class.cast(bean).getTargetClass() : bean.getClass());
	}

	/**
	 * (@Async)创建同步回调或异步回调(Invoker, RunnableInvoker)
	 * 
	 * @param bean
	 * @param method
	 * @return
	 */
	private Invoker generate(Object bean, Method method) {
		return AnnotationUtils.findAnnotation(method, Async.class) == null ? new Invoker(bean, method) : new RunnableInvoker(bean, method);
	}

	private Object binding(Object bean, Method[] methods) {
		for (Method method : methods) {
			com.kepler.annotation.Config config = AnnotationUtils.findAnnotation(method, com.kepler.annotation.Config.class);
			if (config != null) {
				Invoker invoker = this.generate(bean, method);
				// 注册监听指定Key回调
				this.invokers.get(config.value()).add(invoker);
				// 是否立即初始化
				if (config.init()) {
					invoker.invoker(PropertiesUtils.get(config.value()));
				}
				DefaultConfig.LOGGER.info("Config binding: " + config.value());
			}
		}
		return bean;
	}

	private Object binding(Object bean, Class<?> clazz) {
		// 仅标记@Service(服务),@Internal(内部Bean)或开启ALL
		return AnnotationUtils.findAnnotation(clazz, Service.class) != null || AnnotationUtils.findAnnotation(clazz, Internal.class) != null || DefaultConfig.ALL ? this.binding(bean, clazz.getMethods()) : bean;
	}

	/**
	 * 仅当配置变化时进行回调
	 * 
	 * @param configs
	 */
	private void config4compare(Map<String, String> configs) {
		for (String key : configs.keySet()) {
			// Invoker when change
			if (!configs.get(key).equals(PropertiesUtils.get(key))) {
				for (Invoker invoker : this.invokers.get(key)) {
					invoker.invoker(configs.get(key));
				}
			}
		}
	}

	@Override
	public void config(Map<String, String> configs) {
		this.config4compare(configs);
		Map<String, String> current = PropertiesUtils.properties();
		// 内存同步及持久化
		PropertiesUtils.properties(configs);
		// 全局通知
		this.aware.changed(current, configs);
	}

	private class Invoker {

		private final Object object;

		private final Method method;

		private Invoker(Object object, Method method) {
			super();
			this.object = object;
			this.method = method;
			Assert.state(this.method.getParameterTypes().length == 1, "Method: " + method + " must only one parameter ... ");
		}

		public void invoker(String value) {
			try {
				// Method首个参数
				Class<?> request = this.method.getParameterTypes()[0];
				// 如果能使用ConfigParser解析则使用ConfigParser解析, 否则尝试使用基础类型静态ValueOf解析
				this.method.invoke(this.object, DefaultConfig.this.parser.support(request) ? DefaultConfig.this.parser.parse(request, value) : MethodUtils.invokeStaticMethod(DefaultConfig.this.mapping.containsKey(request) ? DefaultConfig.this.mapping.get(request) : request, "valueOf", value));
			} catch (Throwable throwable) {
				DefaultConfig.LOGGER.error("Parameter only allowed byte, short, int, long, boolean, String or using ConfigParser ... ", throwable);
			}
		}
	}

	// Not thread safe
	private class RunnableInvoker extends Invoker implements Runnable {

		/**
		 * 配置快照
		 */
		private String value;

		private RunnableInvoker(Object object, Method method) {
			super(object, method);
		}

		public void invoker(String value) {
			this.value = value;
			DefaultConfig.this.threads.execute(this);
		}

		@Override
		public void run() {
			super.invoker(this.value);
		}
	}

	private class Invokers extends HashMap<String, List<Invoker>> {

		private static final long serialVersionUID = 1L;

		private List<Invoker> get(String key) {
			List<Invoker> invokers = super.get(key);
			if (invokers == null) {
				super.put(key, invokers = new ArrayList<Invoker>());
			}
			return invokers;
		}
	}
}
