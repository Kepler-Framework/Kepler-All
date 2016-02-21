package com.kepler.protocol;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.header.Headers;
import com.kepler.serial.SerialID;
import com.kepler.service.Service;

/**
 * SerialID, 序列化策略
 * 
 * @author kim 2015年7月8日
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public interface Request extends SerialID, Serializable {

	@JsonProperty
	public Service service();

	@JsonProperty
	public String method();

	/**
	 * 精确参数类型
	 * 
	 * @return
	 */
	@JsonProperty
	public Class<?>[] types();

	/**
	 * 实际参数
	 * 
	 * @return
	 */
	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object[] args();

	/**
	 * 是否为异步调用
	 * 
	 * @return
	 */
	public boolean async();

	@JsonProperty
	public Integer ack();

	@JsonProperty
	public Headers headers();

	/**
	 * 快捷代理
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key);

	public String get(String key, String def);

	public Request put(String key, String value);

	public Request putIfAbsent(String key, String value);
}
