package com.kepler.trace.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.admin.trace.impl.TraceTask;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.impl.TraceContext;
import com.kepler.protocol.Request;
import com.kepler.service.Quiet;
import com.kepler.service.Service;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCauses;

/**
 * @author KimShen
 *
 */
public class DefaultCauses implements TraceCauses {

	// Trace数量记录, 最多30条, 默认15条
	private static final int MAX = Math.max(Integer.valueOf(PropertiesUtils.get(DefaultCauses.class.getName().toLowerCase() + ".max", "15")), 30);

	private static final Log LOGGER = LogFactory.getLog(DefaultCauses.class);

	private final Quiet quiet;

	/**
	 * 周期性缓存池 
	 */
	volatile private List<TraceCause> causes_one = new ArrayList<TraceCause>(DefaultCauses.MAX);

	volatile private List<TraceCause> causes_two = new ArrayList<TraceCause>(DefaultCauses.MAX);

	private final AtomicInteger index = new AtomicInteger();

	private final Profile profile;

	public DefaultCauses(Profile profile, Quiet quiet) {
		super();
		this.profile = profile;
		this.quiet = quiet;
	}

	@Override
	public List<TraceCause> get() {
		// 交换并重置缓存
		List<TraceCause> current = this.causes_one;
		this.causes_two.clear();
		this.causes_one = this.causes_two;
		this.causes_two = current;
		this.index.set(0);
		return current;
	}

	/**
	 * 超过限制则跳过
	 * 
	 * @return
	 */
	private boolean allow(Service service, String method, String cause) {
		DefaultCauses.LOGGER.info("[warn-message][service=" + service + "][method=" + method + "][cause=" + cause + "][trace=" + TraceContext.getTrace() + "]");
		return this.index.getAndIncrement() > DefaultCauses.MAX ? false : true;
	}

	public void put(Service service, String method, String cause) {
		if (!this.allow(service, method, cause)) {
			return;
		}
		// 开启收集, 并且为非静默异常
		if (PropertiesUtils.profile(this.profile.profile(service), TraceTask.ENABLED_KEY, TraceTask.ENABLED_DEF)) {
			this.causes_one.add(new DefaultCause(cause, service, method, TraceContext.getTrace()));
		}
	}

	@Override
	public void put(Request request, Throwable throwable) {
		if (!this.allow(request.service(), request.method(), throwable.toString())) {
			return;
		}
		// 开启收集, 并且为非静默异常
		if (PropertiesUtils.profile(this.profile.profile(request.service()), TraceTask.ENABLED_KEY, TraceTask.ENABLED_DEF) && !this.quiet.quiet(request, throwable.getClass())) {
			this.causes_one.add(new DefaultCause(throwable, request.service(), request.method(), TraceContext.getTrace()));
		}
	}
}
