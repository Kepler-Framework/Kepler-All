package com.kepler.generic.reflect.impl;

import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.reflect.GenericArgs;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.method.MethodInfo;
import com.kepler.method.Methods;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;
import com.kepler.service.Quiet;

/**
 * @author KimShen
 *
 */
public class DelegateArray extends DefaultDelegate implements GenericDelegate {

	private final Methods methods;

	public DelegateArray(GenericResponseFactory factory, RequestValidation validation, FieldsAnalyser analyser, Methods methods, Quiet quiet) {
		super(factory, validation, analyser, quiet);
		this.methods = methods;
	}

	@Override
	protected MethodInfo method(Class<?> clazz, String method, Request request) throws Exception {
		// 根据参数匹配的真实方法(使用Instance.class)
		GenericArgs args = GenericArgs.class.cast(request.args()[0]);
		return args.guess() ? this.methods.method(clazz, method, args.args().length) : this.methods.method(clazz, method, args.classes());
	}

	@Override
	protected Object[] args(MethodInfo detail, Request request) throws Exception {
		return GenericArgs.class.cast(request.args()[0]).args();
	}
}