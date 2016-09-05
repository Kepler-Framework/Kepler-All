package com.kepler.admin.status.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.header.impl.TraceContext;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCollector;

/**
 * @author KimShen
 *
 */
public class Status4Trace implements Status {

	private final Map<String, Object> traces = new HashMap<String, Object>();

	private final TraceCollector collector;

	public Status4Trace(TraceCollector collector) {
		super();
		this.collector = collector;
		this.traces.put("traces", new TraceCauses());
	}

	@Override
	public Map<String, Object> get() {
		// 获取并重置
		TraceCauses causes = TraceCauses.class.cast(this.traces.get("traces")).clean();
		for (TraceCause cause : this.collector.get()) {
			if (cause != null) {
				causes.add(cause);
			}
		}
		return this.traces;
	}

	private class TraceCauses extends ArrayList<Map<String, String>> {

		private static final long serialVersionUID = 1L;

		public void add(TraceCause cause) {
			Map<String, String> trace = new HashMap<String, String>();
			// Metadata
			trace.put("service", cause.service().service());
			trace.put("version", cause.service().version());
			trace.put("catalog", cause.service().catalog());
			trace.put("method", cause.method());
			// Trace
			trace.put("trace", TraceContext.get());
			super.add(trace);
		}

		public TraceCauses clean() {
			super.clear();
			return this;
		}
	}
}
