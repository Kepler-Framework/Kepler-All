package com.kepler.token.impl;

import com.kepler.KeplerValidateException;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.protocol.Request;
import com.kepler.token.TokenContext;

/**
 * 需要开启Profile
 * 
 * @author kim
 *
 * 2016年4月20日
 */
public class AccessTokenContext implements TokenContext {

	/**
	 * 获取Profile
	 */
	public static final String TOKEN_PROFILE_KEY = AccessTokenContext.class.getName().toLowerCase() + ".token_profile";

	private static final boolean ENABLED = PropertiesUtils.get(AccessTokenContext.class.getName().toLowerCase() + ".enabled", false);

	/**
	 * 存放Header
	 */
	private static final String TOKEN_HEADER = PropertiesUtils.get(AccessTokenContext.class.getName().toLowerCase() + ".token_header", "token_header");

	private static final String TOKEN_PROFILE_DEF = PropertiesUtils.get(AccessTokenContext.TOKEN_PROFILE_KEY, null);

	private final Profile profile;

	public AccessTokenContext(Profile profile) {
		super();
		this.profile = profile;
	}

	/*
	 * Headers.ENABLED强依赖
	 */
	@Override
	public Request set(Request request, ChannelInvoker invoker) {
		if (Headers.ENABLED && AccessTokenContext.ENABLED) {
			// 获取服务级别的访问Token
			String token = PropertiesUtils.profile(this.profile.profile(request.service()), AccessTokenContext.TOKEN_PROFILE_KEY, AccessTokenContext.TOKEN_PROFILE_DEF);
			request.put(AccessTokenContext.TOKEN_HEADER, token);
		}
		return request;
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		// 如果开启校验
		if (AccessTokenContext.ENABLED && !StringUtils.equals(PropertiesUtils.profile(this.profile.profile(request.service()), AccessTokenContext.TOKEN_PROFILE_KEY, AccessTokenContext.TOKEN_PROFILE_DEF), request.get(AccessTokenContext.TOKEN_HEADER))) {
			throw new KeplerValidateException("Unvalid access token for Request: " + request);
		}
		return request;
	}
}
