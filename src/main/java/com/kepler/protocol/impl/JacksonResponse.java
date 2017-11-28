package com.kepler.protocol.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.protocol.ResponseInner;

/**
 * @author KimShen
 *
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public class JacksonResponse extends DefaultResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JacksonResponse(byte serial, byte[] ack, Object response) {
		super(serial, ack, JacksonResponse.inner(response), null);
	}

	public JacksonResponse(byte serial, byte[] ack, Throwable throwable) {
		super(serial, ack, null, throwable);
	}

	public JacksonResponse(@JsonProperty("serial") byte serial, @JsonProperty("ack") byte[] ack, @JsonProperty("inner") Object response, @JsonProperty("throwable") Throwable throwable) {
		super(serial, ack, JacksonResponse.inner(response), throwable);
	}

	public static Object inner(Object response) {
		// Guard case, 转换为包装Map
		if (Map.class.isAssignableFrom(response.getClass())) {
			return new JacksonInnerMap(Map.class.cast(response));
		}
		// Guard case, 转换为包装Set
		if (Set.class.isAssignableFrom(response.getClass())) {
			return new JacksonInnerSet(Set.class.cast(response));
		}
		// Guard case, 转换为包装List
		if (List.class.isAssignableFrom(response.getClass())) {
			return new JacksonInnerList(List.class.cast(response));
		}
		// Guard case, 转换为包装List
		if (Collection.class.isAssignableFrom(response.getClass())) {
			return new JacksonInnerList(List.class.cast(response));
		}
		return response;
	}

	@JsonProperty
	public byte[] ack() {
		return super.ack();
	}

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object inner() throws Exception {
		return super.response();
	}

	public Object response() {
		// Guard case, Null
		if (super.response() == null) {
			return null;
		}
		// Guard case, 特殊Inner对象处理
		if (ResponseInner.class.isAssignableFrom(super.response().getClass())) {
			return ResponseInner.class.cast(super.response()).inner();
		}
		return super.response();
	}
}
