package com.kepler.router;

import java.util.List;

import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author kim 2015年7月14日
 */
public interface Routing {

	public static final String NAME = "default";
	
	public Host route(Request request, List<Host> hosts);

	public String name();
}
