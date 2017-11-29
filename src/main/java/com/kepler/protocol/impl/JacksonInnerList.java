package com.kepler.protocol.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kepler.config.PropertiesUtils;

/**
 * @author KimShen
 *
 */
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
public class JacksonInnerList extends JacksonInner {

	private static final Log LOGGER = LogFactory.getLog(JacksonInnerList.class);

	private static Class<?> CLAZZ;

	static {
		try {
			JacksonInnerList.CLAZZ = Class.forName(PropertiesUtils.get(JacksonInnerList.class.getName().toLowerCase() + ".class", "java.util.ArrayList"));
		} catch (Exception e) {
			JacksonInnerList.CLAZZ = ArrayList.class;
			JacksonInnerList.LOGGER.error("[collection-init][message=" + e.getMessage() + "]", e);
		}
	}

	public JacksonInnerList() {
		super();
	}

	public JacksonInnerList(List<?> inner) {
		super(inner);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Collection<Object> def() {
		try {
			return (Collection<Object>) JacksonInnerList.CLAZZ.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			JacksonInnerList.LOGGER.error("[collection-init][message=" + e.getMessage() + "]", e);
			return new ArrayList<Object>();
		}
	}
}
