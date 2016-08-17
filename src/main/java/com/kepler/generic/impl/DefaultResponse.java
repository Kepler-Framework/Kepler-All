package com.kepler.generic.impl;

import com.kepler.generic.GenericResponse;

/**
 * @author KimShen
 *
 */
public class DefaultResponse implements GenericResponse {

	private Object response;

	@Override
	public GenericResponse response(Object response) {
		this.response = response;
		return this;
	}

	@Override
	public Object response() {
		return this.response;
	}

	@Override
	public boolean valid() {
		return true;
	}
}
