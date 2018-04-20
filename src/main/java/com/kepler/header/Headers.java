package com.kepler.header;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2015年7月14日
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public interface Headers extends Serializable {

	/**
	 * 是否开启Header
	 */
	public static final boolean ENABLED = PropertiesUtils.get(Headers.class.getName().toLowerCase() + ".enabled", false);
	
	public Headers put(String key, String value);
	
	public Headers put(Map<String, String> headers);

	public Headers putIfAbsent(String key, String value);

	public String get(String key);

	public String get(String key, String def);

	@JsonProperty("headers")
	public Map<String, String> get();

	public Collection<String> keys();
	
	public Headers reset();

	public Headers delete(String key);

	public int length();
}
