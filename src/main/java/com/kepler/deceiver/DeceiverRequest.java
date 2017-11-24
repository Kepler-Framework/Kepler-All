package com.kepler.deceiver;

import java.util.Map;

import com.kepler.generic.reflect.impl.DelegateArgs;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DeceiverRequest {

	private final DelegateArgs args;

	private final Request req;

	public DeceiverRequest(Request request) {
		this.args = DelegateArgs.class.cast(request.args()[0]);
		this.req = request;
	}

	public Map<String, String> getHeader() {
		return this.req.headers().get();
	}

	public String getService() {
		return this.req.service().service();
	}

	public String getVersion() {
		return this.req.service().version();
	}

	public String getCatalog() {
		return this.req.service().catalog();
	}

	public String[] getClasses() {
		return this.args.classesAsString();
	}

	public Object[] getArgs() {
		return this.args.args();
	}
}
