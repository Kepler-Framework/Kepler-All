package com.kepler.generic.reflect.impl;

import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.reflect.GenericBean;
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
public class DelegateMap extends DefaultDelegate implements GenericDelegate {

	private static final String[] NAMES = new String[] {};

	private final Methods methods;

	public DelegateMap(GenericResponseFactory factory, RequestValidation validation, FieldsAnalyser analyser, Methods methods, Quiet quiet) {
		super(factory, validation, analyser, quiet);
		this.methods = methods;
	}

	@Override
	protected MethodInfo method(Object instance, String method, Request request) throws Exception {
		// 根据参数匹配的真实方法(使用Instance.class)
		GenericBean bean = GenericBean.class.cast(request.args()[0]);
		return this.methods.method(instance, method, bean.args() != null ? bean.args().keySet().toArray(new String[] {}) : DelegateMap.NAMES);
	}

	@Override
	protected Object[] args(MethodInfo detail, Request request) throws Exception {
		GenericBean bean = GenericBean.class.cast(request.args()[0]);
		return detail.args(bean.args());
	}
}