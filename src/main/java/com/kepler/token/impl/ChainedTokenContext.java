package com.kepler.token.impl;

import java.util.ArrayList;
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

	private final List<TokenContext> contexts = new ArrayList<TokenContext>();

	public ChainedTokenContext(List<TokenContext> contexts) {
		super();
		for (TokenContext context : contexts) {
			if (context.actived()) {
				this.contexts.add(context);
			}
		}
	}

	@Override
	public boolean actived() {
		return false;
	}

	@Override
	public void set(Request request, ChannelInvoker invoker) {
		if (!this.contexts.isEmpty()) {
			for (TokenContext token : this.contexts) {
				token.set(request, invoker);
			}
		}
	}

	@Override
	public void valid(Request request) throws KeplerValidateException {
		Request temp = request;
		if (!this.contexts.isEmpty()) {
			for (TokenContext token : this.contexts) {
				token.valid(temp);
			}
		}
	}
}
