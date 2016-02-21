package com.kepler.protocol.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.kepler.KeplerLocalException;
import com.kepler.KeplerValidateException;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;

/**
 * @author kim 2015年10月30日
 */
public class Jsr303Validation implements RequestValidation {

	private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private final Scope support;

	public Jsr303Validation(Scope support) {
		super();
		this.support = support;
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		for (Object arg : request.args()) {
			for (ConstraintViolation<Object> valid : Jsr303Validation.VALIDATOR.validate(arg)) {
				throw new KeplerLocalException(valid.getMessage());
			}
		}
		return request;
	}

	public Scope scope() {
		return this.support;
	}
}
