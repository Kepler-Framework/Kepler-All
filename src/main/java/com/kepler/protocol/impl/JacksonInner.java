package com.kepler.protocol.impl;

import java.util.Collection;

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
abstract public class JacksonInner implements ResponseInner {

	/**
	 * 当解析失败是否提示异常
	 */
	private static final boolean WARN = PropertiesUtils.get(JacksonInner.class.getName().toLowerCase() + ".warn", true);

	private static final Log LOGGER = LogFactory.getLog(JacksonInner.class);

	/**
	 * 集合元素
	 */
	private Object[] element;

	/**
	 * 集合类型
	 */
	private Class<?> clazz;

	public JacksonInner() {
		super();
	}

	public JacksonInner(Collection<?> element) {
		super();
		this.element = element.toArray();
		this.clazz = element.getClass();
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> collection(Class<?> clazz) {
		try {
			return (Collection<Object>) this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			if (JacksonInner.WARN) {
				JacksonInner.LOGGER.error("[collection-init]][class=" + clazz + "][message=" + e.getMessage() + "]", e);
			}
			return this.def();
		}
	}

	public Object inner() {
		Collection<Object> source = this.collection(this.clazz);
		for (Object each : this.element) {
			source.add(each);
		}
		return source;
	}

	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
	public Object[] getElememnt() {
		return this.element;
	}

	public void setElement(Object[] element) {
		this.element = element;
	}

	@JsonProperty
	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * 失败时的默认类型
	 * 
	 * @return
	 */
	abstract protected Collection<Object> def();
}
