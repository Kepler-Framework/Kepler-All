package com.kepler.generic.impl;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericInvoker;
import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DefaultDelegate implements GenericDelegate {

	private final GenericResponseFactory factory;

	private final GenericInvoker invoker;

	public DefaultDelegate(GenericResponseFactory factory, GenericInvoker invoker) {
		this.factory = factory;
		this.invoker = invoker;
	}

	@Override
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException {
		return this.invoker.marker().marked(request.headers()) ? this.invoker.delegate().delegate(instance, method, request) : this.factory.unvalid();
	}
}
