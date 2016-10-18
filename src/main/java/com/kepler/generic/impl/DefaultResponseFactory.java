package com.kepler.generic.impl;

import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;

/**
 * @author KimShen
 *
 */
public class DefaultResponseFactory implements GenericResponseFactory {

	private final GenericResponse unvalid = new UnvalidResponse();

	@Override
	public GenericResponse response(Object response) {
		return new DefaultResponse(response);
	}

	@Override
	public GenericResponse unvalid() {
		return this.unvalid;
	}

	private class UnvalidResponse implements GenericResponse {

		private UnvalidResponse() {

		}

		@Override
		public Object response() {
			return null;
		}

		@Override
		public boolean valid() {
			return false;
		}
	}
}
