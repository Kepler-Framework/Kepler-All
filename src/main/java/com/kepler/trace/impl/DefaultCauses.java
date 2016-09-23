package com.kepler.trace.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.admin.trace.impl.TraceTask;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.impl.TraceContext;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.service.Quiet;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCauses;

/**
 * @author KimShen
 *
 */
public class DefaultCauses implements TraceCauses {

	// Trace数量记录, 最多15条, 默认5条
	private static final int MAX = Math.max(Integer.valueOf(PropertiesUtils.get(DefaultCauses.class.getName().toLowerCase() + ".max", "5")), 15);

	private static final Log LOGGER = LogFactory.getLog(DefaultCauses.class);

	private final Quiet quiet;

	private final Host host;

	/**
	 * 周期性缓存池 
	 */
	private volatile List<TraceCause> causes_one = new ArrayList<TraceCause>(DefaultCauses.MAX);

	private volatile List<TraceCause> causes_two = new ArrayList<TraceCause>(DefaultCauses.MAX);

	public DefaultCauses(Host host, Quiet quiet) {
		super();
		this.quiet = quiet;
		this.host = host;
	}

	@Override
	public List<TraceCause> get() {
		// 交换并重置缓存
		List<TraceCause> current = this.causes_one;
		this.causes_two.clear();
		this.causes_one = this.causes_two;
		this.causes_two = current;
		return current;
	}

	@Override
	public void put(Request request, Throwable throwable) {
		// Guard case1, 超过索引立即返回
		if (this.causes_one.size() > DefaultCauses.MAX) {
			DefaultCauses.LOGGER.warn("Array out of range. [max=" + DefaultCauses.MAX + "][index=" + this.causes_one.size() + "]");
			return;
		}
		// 收集非静默异常
		if (TraceTask.ENABLED && !this.quiet.quiet(request, throwable.getClass())) {
			this.causes_one.add(new DefaultCause(throwable, this.host, request.service(), request.method(), TraceContext.get()));
		}
	}
}
