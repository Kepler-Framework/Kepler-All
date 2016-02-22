package com.kepler.header.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 线程不安全
 * 
 * @author kim 2015年7月14日
 */
public class LazyHeaders implements Headers {

	/**
	 * 默认初始容量
	 */
	private final static int CAPACITY = PropertiesUtils.get(LazyHeaders.class.getName().toLowerCase() + ".capacity", 1);

	private final static Collection<String> EMPTY_KEYS = Collections.unmodifiableCollection(new ArrayList<String>());

	private final static Map<String, String> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<String, String>());

	private final static long serialVersionUID = 1L;

	private Map<String, String> headers;

	public LazyHeaders() {

	}

	public LazyHeaders(@JsonProperty("headers") Map<String, String> headers) {
		// 如果为空则指定为Null(惰性)
		this.headers = headers.isEmpty() ? null : headers;
	}

	private boolean initial(String key, String value) {
		if (this.headers == null) {
			// 初始化, 赋值并返回
			(this.headers = new HashMap<String, String>(LazyHeaders.CAPACITY)).put(key, value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public LazyHeaders put(String key, String value) {
		if (!this.initial(key, value)) {
			this.headers.put(key, value);
		}
		return this;
	}

	public LazyHeaders putIfAbsent(String key, String value) {
		if (!this.initial(key, value) && !this.headers.containsKey(key)) {
			this.headers.put(key, value);
		}
		return this;
	}

	@Override
	public String get(String key) {
		return this.headers == null ? null : this.headers.get(key);
	}

	public String get(String key, String def) {
		return StringUtils.defaultString(this.get(key), def);
	}

	public Map<String, String> get() {
		return this.headers == null ? LazyHeaders.EMPTY_MAP : this.headers;
	}

	public Collection<String> keys() {
		return this.headers == null ? LazyHeaders.EMPTY_KEYS : this.headers.keySet();
	}

	public int length() {
		return this.headers == null ? 0 : this.headers.size();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
