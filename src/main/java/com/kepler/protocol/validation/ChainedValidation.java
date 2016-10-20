package com.kepler.protocol.validation;

import java.util.ArrayList;
import java.util.List;

import com.kepler.KeplerValidateException;
import com.kepler.extension.Extension;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;

/**
 * @author kim 2015年10月30日
 */
public class ChainedValidation implements RequestValidation, Extension {

	private final List<RequestValidation> validations = new ArrayList<RequestValidation>();

	private final Scope scope;

	public ChainedValidation(Scope scope) {
		super();
		this.scope = scope;
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		Request actual = request;
		if (!this.validations.isEmpty()) {
			for (RequestValidation each : this.validations) {
				actual = each.valid(request);
			}
		}
		return actual;
	}

	@Override
	public ChainedValidation install(Object instance) {
		// 加载Client或Service配置
		RequestValidation validation = RequestValidation.class.cast(instance);
		if (Scope.ALL.equals(validation.scope()) || this.scope.equals(validation.scope())) {
			this.validations.add(validation);
		}
		return this;
	}

	@Override
	public Class<?> interested() {
		return RequestValidation.class;
	}

	public Scope scope() {
		return Scope.ALL;
	}
}
