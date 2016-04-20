package com.kepler.token.impl;

import java.util.List;

import com.kepler.KeplerValidateException;
import com.kepler.channel.ChannelInvoker;
import com.kepler.protocol.Request;
import com.kepler.token.TokenContext;

/**
 * @author kim
 *
 * 2016年4月20日
 */
public class ChainedTokenContext implements TokenContext {

	private final List<TokenContext> contexts;

	public ChainedTokenContext(List<TokenContext> contexts) {
		super();
		this.contexts = contexts;
	}

	@Override
	public Request set(Request request, ChannelInvoker invoker) {
		Request temp = request;
		for (TokenContext token : this.contexts) {
			temp = token.set(temp, invoker);
		}
		return temp;
	}

	@Override
	public Request valid(Request request) throws KeplerValidateException {
		Request temp = request;
		for (TokenContext token : this.contexts) {
			temp = token.valid(temp);
		}
		return temp;
	}
}
