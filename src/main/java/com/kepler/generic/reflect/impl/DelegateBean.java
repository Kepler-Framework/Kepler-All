package com.kepler.generic.reflect.impl;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.generic.reflect.GenericBean;

/**
 * @author KimShen
 *
 */
public class DelegateBean implements GenericBean {

	private static final long serialVersionUID = 1L;

	@JsonProperty
	private final LinkedHashMap<String, Object> args;

	public DelegateBean(@JsonProperty("args") LinkedHashMap<String, Object> args) {
		super();
		this.args = args;
	}

	@Override
	public LinkedHashMap<String, Object> args() {
		return this.args;
	}
}
