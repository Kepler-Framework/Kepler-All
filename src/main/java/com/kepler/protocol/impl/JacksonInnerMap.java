package com.kepler.protocol.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.config.PropertiesUtils;
import com.kepler.protocol.ResponseInner;

/**
 * @author KimShen
 *
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public class JacksonInnerMap implements ResponseInner {

	private static final boolean WARN = PropertiesUtils.get(JacksonInnerMap.class.getName().toLowerCase() + ".warn", true);

	private static final Log LOGGER = LogFactory.getLog(JacksonInnerMap.class);

	private static Class<?> CLAZZ;

	static {
		try {
			JacksonInnerMap.CLAZZ = Class.forName(PropertiesUtils.get(JacksonInnerList.class.getName().toLowerCase() + ".class", "java.util.HashMap"));
		} catch (Exception e) {
			JacksonInnerMap.CLAZZ = HashMap.class;
			JacksonInnerMap.LOGGER.error("[collection-init][message=" + e.getMessage() + "]", e);
		}
	}

	private Class<?> clazz;

	private Object[] value;

	private Object[] key;

	public JacksonInnerMap() {
		super();
	}

	public JacksonInnerMap(Map<?, ?> inner) {
		super();
		this.value = new Object[inner.size()];
		this.key = new Object[inner.size()];
		this.clazz = inner.getClass();
		int index = 0;
		for (Object key : inner.keySet()) {
			this.value[index] = inner.get(key);
			this.key[index] = key;
			index++;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> map(Class<?> clazz) {
		try {
			return (Map<Object, Object>) this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			if (JacksonInnerMap.WARN) {
				JacksonInnerMap.LOGGER.error("[collection-init]][class=" + clazz + "][message=" + e.getMessage() + "]", e);
			}
			try {
				return (Map<Object, Object>) JacksonInnerMap.CLAZZ.newInstance();
			} catch (InstantiationException | IllegalAccessException inner) {
				JacksonInnerMap.LOGGER.error("[collection-init]][class=" + clazz + "][message=" + inner.getMessage() + "]", inner);
				return new HashMap<Object, Object>();
			}
		}
	}

	public Object inner() {
		Map<Object, Object> source = this.map(this.clazz);
		for (int index = 0; index < this.key.length; index++) {
			source.put(this.key[index], this.value[index]);
		}
		return source;
	}

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object[] getKey() {
		return this.key;
	}

	public void setKey(Object[] key) {
		this.key = key;
	}

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object[] getValue() {
		return this.value;
	}

	public void setValue(Object[] value) {
		this.value = value;
	}

	@JsonProperty
	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
}
