package com.kepler.admin.status.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.header.impl.TraceContext;
import com.kepler.trace.TraceCause;
import com.kepler.trace.TraceCauses;

/**
 * @author KimShen
 *
 */
public class Status4Trace implements Status {

	private final Map<String, Object> traces = new HashMap<String, Object>();

	private final TraceCauses causes;

	public Status4Trace(TraceCauses causes) {
		super();
		this.causes = causes;
		this.traces.put("traces", new TraceCausesAsMap());
	}

	@Override
	public Map<String, Object> get() {
		// 获取并重置
		TraceCausesAsMap causes = TraceCausesAsMap.class.cast(this.traces.get("traces")).clean();
		for (TraceCause cause : this.causes.get()) {
			if (cause != null) {
				causes.add(cause);
			}
		}
		return this.traces;
	}

	private class TraceCausesAsMap extends ArrayList<Map<String, String>> {

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

		public TraceCausesAsMap clean() {
			super.clear();
			return this;
		}
	}
}
