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

	public static final String TRACE = "trace";

	public static final String TRACE_COVER = "trace_to_cover";

	public static final String SPAN = "span";

	public static final String SPAN_PARENT = "parent_span";

	public static final String START_TIME = "start_time";

	/**
	 * @param request 请求
	 * @param response 响应
	 * @param local 本地主机
	 * @param remote 远程主机
	 * @param waiting 请求等待时间
	 * @param elapse 请求消耗时间
	 * @param received 请求接受时间
	 */
	public void trace(Request request, Response response, String local, String remote, long waiting, long elapse, long received);
}
