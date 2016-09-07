package com.kepler.trace.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.kepler.admin.trace.impl.TraceTask;
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

	/**
	 * 周期性缓存池 
	 */
	private final TraceCause[] traces = new TraceCause[DefaultCauses.MAX + 1];

	private final List<TraceCause> causes = new ArrayList<TraceCause>();

	private final AtomicInteger index = new AtomicInteger();

	private final Quiet quiet;

	public DefaultCauses(Quiet quiet) {
		super();
		this.reset();
		this.quiet = quiet;
	}

	/**
	 * 重置为Null
	 */
	private void reset() {
		for (int index = 0; index < this.traces.length; index++) {
			this.traces[index] = null;
		}
	}

	@Override
	public List<TraceCause> get() {
		this.causes.clear();
		// 追加集合
		for (TraceCause each : this.traces) {
			if (each != null) {
				this.causes.add(each);
			}
		}
		// 重置当前收集器
		this.reset();
		return this.causes;
	}

	@Override
	public void put(Request request, Throwable throwable) {
		// 收集非静默异常
		if (TraceTask.ENABLED && !this.quiet.quiet(request, throwable.getClass())) {
			this.traces[this.index.incrementAndGet() & DefaultCauses.MAX] = new DefaultCause(request.service(), request.method(), TraceContext.get());
		}
	}
}
