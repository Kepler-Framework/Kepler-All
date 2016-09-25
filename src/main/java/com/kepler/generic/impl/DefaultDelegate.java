package com.kepler.generic.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log LOGGER = LogFactory.getLog(DefaultDelegate.class);

	private final List<GenericInvoker> invokers = new ArrayList<GenericInvoker>();

	private final GenericResponseFactory factory;

	private final boolean empty;

	public DefaultDelegate(GenericResponseFactory factory, List<GenericInvoker> invokers) {
		// 仅加载激活的代理
		for (GenericInvoker each : invokers) {
			if (each.actived()) {
				this.invokers.add(each);
			}
		}
		if (this.invokers.isEmpty()) {
			DefaultDelegate.LOGGER.info("Generic delegate was closed");
		}
		this.empty = this.invokers.isEmpty();
		this.factory = factory;
	}

	@Override
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException {
		// 未开启泛化立即返回
		if (this.empty) {
			return this.factory.unvalid();
		}
		for (GenericInvoker invoker : this.invokers) {
			// 如果Header标记支持则调用
			if (invoker.marker().marked(request.headers())) {
				return invoker.delegate().delegate(instance, method, request);
			}
		}
		// 非泛化请求
		return this.factory.unvalid();
	}
}
