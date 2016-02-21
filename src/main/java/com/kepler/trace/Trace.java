package com.kepler.trace;

import com.kepler.config.PropertiesUtils;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;

/**
 * 请求跟踪
 * 
 * @author kim 2015年12月24日
 */
public interface Trace {

	public final static String ENABLED_KEY = Trace.class.getName().toLowerCase() + ".enabled";

	public final static boolean ENABLED_DEF = PropertiesUtils.get(Trace.ENABLED_KEY, false);

	public final static String TRACE = "trace";

	public void trace(Request request, Response response, String local, String remote, long waiting, long elapse);
}
