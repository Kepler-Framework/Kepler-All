package com.kepler.generic.impl;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.kepler.generic.GenericArg;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericMarker;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class DefaultDelegate implements GenericMarker, GenericDelegate {

	/**
	 * Header Key, 用于服务端判定
	 */
	private static final String DELEGATE_KEY = DefaultDelegate.class.getName().toLowerCase() + ".delegate";

	private static final String DELEGATE_VAL = "";

	private static final String PREFIX = "get";

	@Override
	public boolean marked(Headers headers) {
		// 从Header中获取并对比
		return StringUtils.equals(headers.get(DefaultDelegate.DELEGATE_KEY), DefaultDelegate.DELEGATE_VAL);
	}

	@Override
	public Headers mark(Headers headers) {
		return headers.put(DefaultDelegate.DELEGATE_KEY, DefaultDelegate.DELEGATE_VAL);
	}

	@Override
	public Object delegate(Object service, String method, Object... args) throws Throwable {
		// 代理执行
		return MethodUtils.invokeMethod(service, method, new Args(args).args());
	}

	/**
	 * 如果类型兼容则直接返回, 如果不兼容则判断是否为Map或其子类, 如果为Map子类则进行代理, 否则返回Null
	 * @param arg
	 * @param 预计类型
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Object arg(Object arg, Class<?> expect) throws Exception {
		return arg.getClass().isAssignableFrom(expect) ? arg : Map.class.isAssignableFrom(arg.getClass()) ? new CglibProxy(Map.class.cast(arg)).getProxy(expect) : null;
	}

	/**
	 * GenericArg转换为实际参数
	 * 
	 * @author KimShen
	 *
	 */
	private class Args {

		private final Object[] args;

		private Args(Object... args) throws Exception {
			// 标齐长度
			this.args = new Object[args.length];
			for (int index = 0; index < args.length; index++) {
				GenericArg arg = GenericArg.class.cast(args[index]);
				this.args[index] = DefaultDelegate.this.arg(arg.arg(), arg.clazz());
			}
		}

		/**
		 * 获取实际参数
		 * 
		 * @return
		 */
		public Object[] args() {
			return this.args;
		}
	}

	/**
	 * 参数代理
	 * 
	 * @author KimShen
	 *
	 */
	private class CglibProxy implements MethodInterceptor {

		private final Enhancer enhancer = new Enhancer();

		private final Map<String, Object> object;

		private CglibProxy(Map<String, Object> object) {
			super();
			this.object = object;
		}

		public Object getProxy(Class<?> clazz) {
			this.enhancer.setCallback(this);
			this.enhancer.setSuperclass(clazz);
			return this.enhancer.create();
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			// GetName -> name
			Object arg = this.object.get(method.getName().replaceFirst(DefaultDelegate.PREFIX, "").toLowerCase());
			return DefaultDelegate.this.arg(arg, method.getReturnType());
		}
	}
}
