package com.kepler.header.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;

/**
 * @author KimShen
 *
 */
public class UserContext {

	/**
	 * 传递时Header.key
	 */
	private static final String KEY = PropertiesUtils.get(UserContext.class.getName().toLowerCase() + ".key", "user");

	private static final String DEF = PropertiesUtils.get(UserContext.class.getName().toLowerCase() + ".key", null);

	private static final Log LOGGER = LogFactory.getLog(UserContext.class);

	public static String get() {
		// 如果上下文不存在用户信息则使用DEF
		return UserContext.get(UserContext.DEF);
	}

	public static String get(String def) {
		Headers headers = ThreadHeaders.HEADERS.get();
		return headers != null ? headers.get(UserContext.KEY) : def;
	}

	public static void set(String user) {
		Headers headers = ThreadHeaders.HEADERS.get();
		// 上下文存在Headers则指定用户
		if (headers != null) {
			headers.put(UserContext.KEY, user);
		} else {
			UserContext.LOGGER.warn("Can not set [user=" + user + "] for headers(null)");
		}
	}
}
