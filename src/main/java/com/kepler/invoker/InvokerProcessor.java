package com.kepler.invoker;

import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface InvokerProcessor {

	public Request before(Request request, Host host);
}
