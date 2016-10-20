package com.kepler.token.impl;

import com.kepler.KeplerValidateException;
import com.kepler.channel.ChannelInvoker;
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
public class ConnectTokenContext implements TokenContext {

	private static final boolean ENABLED = PropertiesUtils.get(ConnectTokenContext.class.getName().toLowerCase() + ".enabled", false);

	@Override
	public boolean actived() {
		return Headers.ENABLED && ConnectTokenContext.ENABLED;
	}

	/*
	 * Headers.ENABLED强依赖
	 */
	@Override
	public Request set(Request request, ChannelInvoker invoker) {
		if (Headers.ENABLED && ConnectTokenContext.ENABLED) {
			request.put(Host.TOKEN_KEY, invoker.host().token());
		}
		return request;
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		// 如果开启校验
		if (ConnectTokenContext.ENABLED && !Host.TOKEN_VAL.equals(request.get(Host.TOKEN_KEY))) {
			throw new KeplerValidateException("Unvalid connect token for Request: " + request);
		}
		return request;
	}
}
