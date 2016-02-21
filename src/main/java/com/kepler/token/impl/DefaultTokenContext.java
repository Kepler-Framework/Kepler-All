package com.kepler.token.impl;

import com.kepler.KeplerValidateException;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.token.TokenContext;

/**
 * @author kim
 *
 * 2016年2月18日
 */
public class DefaultTokenContext implements TokenContext {

	private final static boolean ENABLED = PropertiesUtils.get(DefaultTokenContext.class.getName().toLowerCase() + ".enabled", false);

	/*
	 * Headers.ENABLED强依赖
	 */
	@Override
	public Request set(Request request, String token) {
		if (Headers.ENABLED && DefaultTokenContext.ENABLED) {
			request.put(Host.TOKEN_KEY, token);
		}
		return request;
	}

	@Override
	public Request valid(Request request, String token) throws KeplerValidateException {
		// 如果开启校验
		if (DefaultTokenContext.ENABLED && !Host.TOKEN_VAL.equals(request.get(Host.TOKEN_KEY))) {
			throw new KeplerValidateException("Unvalid token for Request: " + request);
		}
		return request;
	}
}
