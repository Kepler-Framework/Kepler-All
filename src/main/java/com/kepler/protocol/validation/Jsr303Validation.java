package com.kepler.protocol.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.kepler.KeplerValidateException;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;

/**
 * @author kim 2015年10月30日
 */
public class Jsr303Validation implements RequestValidation {

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private final Scope support;

	public Jsr303Validation(Scope support) {
		super();
		this.support = support;
	}

	/**
	 * 组合异常并抛出
	 * 
	 * @param exceptions
	 */
	private void appendAndThrow(Set<ConstraintViolation<Object>> exceptions) {
		StringBuffer buffer = new StringBuffer();
		for (ConstraintViolation<Object> valid : exceptions) {
			// [属性路径1=异常信息] [属性路径2=异常信息]
			buffer.append("[").append(valid.getPropertyPath()).append("=").append(valid.getMessage() + "]");
		}
		throw new KeplerValidateException(buffer.toString());
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		for (Object arg : request.args()) {
			if (arg != null) {
				Set<ConstraintViolation<Object>> exceptions = Jsr303Validation.VALIDATOR.validate(arg);
				// 如果存在异常则开始拼装
				if (!exceptions.isEmpty()) {
					this.appendAndThrow(exceptions);
				}
			}
		}
		return request;
	}

	public Scope scope() {
		return this.support;
	}
}
