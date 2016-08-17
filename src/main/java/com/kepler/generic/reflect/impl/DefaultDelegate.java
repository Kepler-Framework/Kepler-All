package com.kepler.generic.reflect.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.kepler.KeplerGenericException;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericInvoker;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.impl.DefaultMarker;
import com.kepler.generic.reflect.GenericArgs;
import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.org.apache.commons.collections.keyvalue.MultiKey;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DefaultDelegate extends DefaultMarker implements GenericMarker, GenericInvoker, GenericDelegate {

	private static final boolean ACTIVED = PropertiesUtils.get(DefaultDelegate.class.getName().toLowerCase() + ".actived", false);

	/**
	 * 是否缓存推导方法
	 */
	private static final boolean CACHED = PropertiesUtils.get(DefaultDelegate.class.getName().toLowerCase() + ".cached", true);

	/**
	 * Header Key, 用于服务端判定
	 */
	private static final String DELEGATE_KEY = DefaultDelegate.class.getName().toLowerCase() + ".delegate";

	private static final String DELEGATE_VAL = "generic_reflect";

	private static final Object[] EMPTY = new Object[] { null };

	private final GenericResponseFactory factory;

	private final FieldsAnalyser analyser;

	/**
	 * 缓存推导方法
	 */
	private volatile MultiKeyMap cache = new MultiKeyMap();

	public DefaultDelegate(GenericResponseFactory factory, FieldsAnalyser analyser) {
		super();
		this.analyser = analyser;
		this.factory = factory;
	}

	protected String key() {
		return DefaultDelegate.DELEGATE_KEY;
	}

	protected String value() {
		return DefaultDelegate.DELEGATE_VAL;
	}

	@Override
	public DefaultDelegate marker() {
		return this;
	}

	public boolean actived() {
		return DefaultDelegate.ACTIVED;
	}

	@Override
	public DefaultDelegate delegate() {
		return this;
	}

	@Override
	public GenericResponse delegate(Object service, String method, Request request) throws KeplerGenericException {
		return this.delegate(service, method, GenericArgs.class.cast(request.args()[0]));
	}

	private GenericResponse delegate(Object service, String method, GenericArgs args) throws KeplerGenericException {
		try {
			// 根据参数匹配的真实方法
			Method method_actual = this.method(service.getClass(), method, args);
			// Guard case, 唯一参数且为Null
			if (args.args() == null) {
				return this.factory.response(method_actual.invoke(service, DefaultDelegate.EMPTY));
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
			return this.factory.response(method_actual.invoke(service, args_actual));
		} catch (InvocationTargetException e) {
			// 处理Method实际错误
			throw new KeplerGenericException(e.getTargetException());
		} catch (Throwable e) {
			// 泛化调用统一返回KeplerGenericException
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
	private Method method(Class<?> service, String method, GenericArgs args) throws Exception {
		// 获取推导方法
		Method method_guess = this.method4guess(service, method, args);
		// 如果无推导则尝试获取实际方法
		Method method_actual = method_guess != null ? method_guess : MethodUtils.getAccessibleMethod(service, method, args.classes());
		if (method_actual == null) {
			throw new NoSuchMethodException(method);
		}
		return method_actual;
	}

	/**
	 * 方法推导
	 * 
	 * @param service
	 * @param method
	 * @param args
	 * @return
	 */
	private Method method4guess(Class<?> service, String method, GenericArgs args) throws Exception {
		// 判断是否需要推导
		if (args.guess()) {
			// Guard case, 如果存在缓存则返回
			Method cached = this.method4cached(service, method, args.args().length);
			if (cached != null) {
				return cached;
			}
			// 否则尝试获取方法并放入缓存
			return this.method2cached(service, method, args.args().length);
		}
		// 无需推导
		return null;
	}

	/**
	 * 从缓存获取Mehtod
	 * 
	 * @param service
	 * @param method
	 * @param args
	 * @return
	 */
	private Method method4cached(Class<?> service, String method, int length) {
		// 开启缓存则尝试获取
		if (DefaultDelegate.CACHED) {
			return Method.class.cast(this.cache.get(service, method, length));
		}
		return null;
	}

	/**
	 * 获取Method放入缓存
	 * 
	 * @param service
	 * @param method
	 * @param length
	 * @return
	 */
	private Method method2cached(Class<?> service, String method, int length) throws Exception {
		// 尝试遍历方法
		for (Method each : service.getMethods()) {
			// 名称相等并且参数类型相等则判定相同
			if (each.getName().equals(method) && each.getParameterTypes().length == length) {
				return DefaultDelegate.CACHED ? this.replace4cached(service, method, length, each) : each;
			}
		}
		throw new NoSuchMethodException("No such method: " + method + " for class: " + service + "[args=" + length + "]");
	}

	/**
	 * 替换缓存
	 * 
	 * @param service
	 * @param method
	 * @param length
	 * @param actual
	 * @return
	 */
	private Method replace4cached(Class<?> service, String method, int length, Method actual) {
		synchronized (actual) {
			// 同步检查
			if (this.cache.containsKey(service, method, length)) {
				return actual;
			}
			MultiKeyMap cached = new MultiKeyMap();
			// 复制
			for (Object key : this.cache.keySet()) {
				MultiKey key_ = MultiKey.class.cast(key);
				cached.put(key_.getKey(0), key_.getKey(1), key_.getKey(2), this.cache.get(key));
			}
			// 放入新缓存并替换
			cached.put(service, method, length, method);
			this.cache = cached;
		}
		return actual;
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