package com.kepler.async;

import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface AsyncRunner {

	public boolean execute(Request request, AsyncWriter writer);
}
