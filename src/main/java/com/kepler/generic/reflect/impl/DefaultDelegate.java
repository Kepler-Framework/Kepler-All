package com.kepler.generic.reflect.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerGenericException;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.method.MethodInfo;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;
import com.kepler.service.Quiet;

/**
 * @author KimShen
 *
 */
abstract public class DefaultDelegate implements GenericDelegate {

	public static final String DELEGATE_KEY = DefaultDelegate.class.getName().toLowerCase() + ".delegate";

	public static final String DELEGATE_VAL = PropertiesUtils.get(DefaultDelegate.DELEGATE_KEY, "generic_reflect");

	private static final Log LOGGER = LogFactory.getLog(DefaultDelegate.class);

	private final GenericResponseFactory factory;

	private final RequestValidation validation;

	private final FieldsAnalyser analyser;

	private final Quiet quiet;

	public DefaultDelegate(GenericResponseFactory factory, RequestValidation validation, FieldsAnalyser analyser, Quiet quiet) {
		this.validation = validation;
		this.analyser = analyser;
		this.factory = factory;
		this.quiet = quiet;
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

	/**
	 * 是否输出异常(或静默)
	 */
	private void exception(Object instance, Request request, String method, Throwable e) {
		try {
			this.quiet.print(request, this.method(instance, method, request).method(), e);
		} catch (Exception inner) {
			DefaultDelegate.LOGGER.debug(inner.getMessage(), inner);
			DefaultDelegate.LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException {
		try {
			MethodInfo info = this.method(instance, method, request);
			Object[] args = this.args(info, request);
			// Method对应Fields集合
			Fields[] fields_all = this.fields(info.method());
			for (int index = 0; index < fields_all.length; index++) {
				Fields fields = fields_all[index];
				// 如果参数列表为Null或当前值为Null则使用Null,否则尝试解析
				args[index] = args[index] == null ? null : fields.actual(args[index]);
			}
			// 代理执行
			return this.factory.response(info.method().invoke(instance, this.validation.valid(args)));
		} catch (InvocationTargetException e) {
			this.exception(instance, request, method, e);
			throw new KeplerGenericException(e.getTargetException());
		} catch (NoSuchMethodException e) {
			// NoSucheMethod无视静默
			DefaultDelegate.LOGGER.error(e.getMessage(), e);
			throw new KeplerGenericException(e.getMessage());
		} catch (Throwable e) {
			this.exception(instance, request, method, e);
			throw new KeplerGenericException(e);
		}
	}

	abstract MethodInfo method(Object instance, String method, Request request) throws Exception;

	abstract Object[] args(MethodInfo info, Request request) throws Exception;
}