package com.kepler.protocol;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.serial.SerialID;

/**
 * SerialID, 序列化策略
 * 
 * @author kim 2015年7月8日
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public interface Response extends SerialID, Serializable {

	@JsonProperty
	public byte[] ack();

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object response();

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Throwable throwable();

	/**
	 * 是否存在Exception
	 * 
	 * @return
	 */
	public boolean valid();
}
