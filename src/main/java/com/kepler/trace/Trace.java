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

	public static final String ENABLED_KEY = Trace.class.getName().toLowerCase() + ".enabled";

	public static final boolean ENABLED_DEF = PropertiesUtils.get(Trace.ENABLED_KEY, false);

	public static final String PARENT_SPAN = "parent_span";

	public static final String SPAN = "span";

	public static final String START_TIME = "start_time";

	public void trace(Request request, Response response, String local, String remote, long waiting, long elapse);
}
