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

	@Override
	public void put(Request request, Throwable throwable) {
		// Guard case, 超过限制则跳过
		if (this.index.getAndIncrement() > DefaultCauses.MAX) {
			DefaultCauses.LOGGER.warn("Array out of range. [max=" + DefaultCauses.MAX + "][index=" + this.causes_one.size() + "]");
			return;
		}
		// 开启收集, 并且为非静默异常
		if (PropertiesUtils.profile(this.profile.profile(request.service()), TraceTask.ENABLED_KEY, TraceTask.ENABLED_DEF) && !this.quiet.quiet(request, throwable.getClass())) {
			this.causes_one.add(new DefaultCause(throwable, request.service(), request.method(), TraceContext.get()));
		}
	}
}
