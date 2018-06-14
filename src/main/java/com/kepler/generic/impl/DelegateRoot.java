package com.kepler.generic.impl;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.reflect.GenericArgs;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.generic.reflect.impl.DefaultDelegate;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegateRoot implements GenericDelegate {

	private final GenericResponseFactory factory;

	private final GenericDelegate bean;

	private final GenericDelegate args;

	public DelegateRoot(GenericResponseFactory factory, GenericDelegate bean, GenericDelegate args) {
		super();
		this.factory = factory;
		this.bean = bean;
		this.args = args;
	}

	@Override
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException {
		// Header校验
		if (!StringUtils.equals(request.get(DefaultDelegate.DELEGATE_KEY), DefaultDelegate.DELEGATE_VAL)) {
			return this.factory.unvalid();
		}
		// 泛化分发
		Object args = request.args()[0];
		request.headers().delete(DefaultDelegate.DELEGATE_KEY);
		if (GenericBean.class.isAssignableFrom(args.getClass())) {
			return this.bean.delegate(instance, method, request);
		}
		if (GenericArgs.class.isAssignableFrom(args.getClass())) {
			return this.args.delegate(instance, method, request);
		}
		throw new KeplerGenericException("Unsupported request");
	}
}
