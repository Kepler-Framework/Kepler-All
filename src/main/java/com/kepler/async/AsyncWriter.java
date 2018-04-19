package com.kepler.async;

import com.kepler.protocol.Request;
import com.kepler.protocol.Response;

/**
 * @author KimShen
 *
 */
public interface AsyncWriter {

	public void write(Request request, Response response) throws Exception;
}
