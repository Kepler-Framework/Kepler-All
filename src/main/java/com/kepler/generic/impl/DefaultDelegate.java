package com.kepler.generic.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArgs;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.analyse.Fields;
import com.kepler.generic.analyse.FieldsAnalyser;
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

	private static final Object[] EMPTY = new Object[] { null };

	private static final String DELEGATE_VAL = "";

	private final FieldsAnalyser analyser;

	public DefaultDelegate(FieldsAnalyser analyser) {
		super();
		this.analyser = analyser;
	}

	@Override
	public boolean marked(Headers headers) {
		try {
			// 从Header中获取并对比
			return headers != null && StringUtils.equals(headers.get(DefaultDelegate.DELEGATE_KEY), DefaultDelegate.DELEGATE_VAL);
		} finally {
			if (headers != null) {
				// 清空Header防止调用链错误
				headers.put(DefaultDelegate.DELEGATE_KEY, null);
			}
		}
	}

	@Override
	public Headers mark(Headers headers) {
		return headers.put(DefaultDelegate.DELEGATE_KEY, DefaultDelegate.DELEGATE_VAL);
	}

	@Override
	public Object delegate(Object service, String method, GenericArgs args) throws KeplerGenericException {
		try {
			// 根据参数匹配的真实方法
			Method method_actual = this.method(service.getClass(), method, args.classes());
			// Guard case, 唯一参数且为Null
			if (args.args() == null) {
				return method_actual.invoke(service, DefaultDelegate.EMPTY);
			}
			// 实际参数
			Object[] args_actual = new Object[args.args().length];
			// Method对应Fields集合
			Fields[] fields_all = this.fields(method_actual);
			for (int index = 0; index < fields_all.length; index++) {
				Fields fields = fields_all[index];
				// 如果为Null则使用Null,否则尝试解析
				args_actual[index] = args.args()[index] == null ? null : fields.actual(args.args()[index]);
			}
			// 代理执行
			return method_actual.invoke(service, args_actual);
		} catch (InvocationTargetException e) {
			// 处理Method实际错误
			throw new KeplerGenericException(e.getTargetException());
		} catch (Throwable e) {
			throw new KeplerGenericException(e);
		}
	}

	/**
	 * 获取Method, 如果不存在则抛出异常
	 * 
	 * @param service
	 * @param method
	 * @param classes
	 * @return
	 * @throws Exception
	 */
	private Method method(Class<?> service, String method, Class<?>[] classes) throws Exception {
		Method method_actual = MethodUtils.getAccessibleMethod(service, method, classes);
		if (method_actual == null) {
			throw new NoSuchMethodException(method);
		}
		return method_actual;
	}

	/**
	 * 获取Fields, 如果不存在则抛出异常
	 * 
	 * @param method
	 * @return
	 * @throws Exception
	 */
	private Fields[] fields(Method method) throws Exception {
		Fields[] fields = this.analyser.get(method);
		if (fields == null) {
			throw new KeplerGenericException("No such analyser for: " + method + ", please set @Generic");
		}
		return fields;
	}
}