package com.kepler.generic.impl;

import com.kepler.generic.GenericResponse;

/**
 * @author KimShen
 *
 */
public class DefaultResponse implements GenericResponse {

	private Object response;

	public DefaultResponse(Object response) {
		this.response = response;
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
