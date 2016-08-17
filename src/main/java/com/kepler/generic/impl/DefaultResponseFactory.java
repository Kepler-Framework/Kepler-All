package com.kepler.generic.impl;

import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;

/**
 * @author KimShen
 *
 */
public class DefaultResponseFactory implements GenericResponseFactory {

	private final GenericResponse unvalid = new UnvalidResponse();

	/*可复用Response*/
	private static final ThreadLocal<GenericResponse> RESPONSE = new ThreadLocal<GenericResponse>() {
		protected GenericResponse initialValue() {
			return new DefaultResponse();
		}
	};

	@Override
	public GenericResponse response(Object response) {
		return DefaultResponseFactory.RESPONSE.get().response(response);
	}

	@Override
	public GenericResponse unvalid() {
		return this.unvalid;
	}

	private class UnvalidResponse implements GenericResponse {

		private UnvalidResponse() {

		}

		@Override
		public GenericResponse response(Object response) {
			return null;
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
