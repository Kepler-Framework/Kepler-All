package com.kepler.protocol.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.config.PropertiesUtils;

/**
 * @author KimShen
 *
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public class JacksonInnerSet extends JacksonInner {

	private static final Logger LOGGER = Logger.getLogger(JacksonInnerSet.class);

	private static Class<?> CLAZZ;

	static {
		try {
			JacksonInnerSet.CLAZZ = Class.forName(PropertiesUtils.get(JacksonInnerSet.class.getName().toLowerCase() + ".class", "java.util.HashSet"));
		} catch (Exception e) {
			JacksonInnerSet.CLAZZ = HashSet.class;
			JacksonInnerSet.LOGGER.error("[collection-init][message=" + e.getMessage() + "]", e);
		}
	}

	public JacksonInnerSet() {
		super();
	}

	public JacksonInnerSet(Set<?> inner) {
		super(inner);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Collection<Object> def() {
		try {
			return (Collection<Object>) JacksonInnerSet.CLAZZ.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			JacksonInnerSet.LOGGER.error("[collection-init][message=" + e.getMessage() + "]", e);
			return new HashSet<Object>();
		}
	}
}
