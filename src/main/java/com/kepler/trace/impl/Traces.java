package com.kepler.trace.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.extension.Extension;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.trace.Trace;

/**
 * @author kim 2015年12月24日
 */
public class Traces implements Trace, Extension {

	private static final Log LOGGER = LogFactory.getLog(Traces.class);

	private final List<Trace> traces = new ArrayList<Trace>();

	@Override
	public Traces install(Object instance) {
		this.traces.add(Trace.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return Trace.class;
	}

	@Override
	public void trace(Request request, Response response, String local, String remote, long waiting, long elapse, long received) {
		if (!this.traces.isEmpty()) {
			for (Trace each : this.traces) {
				try {
					each.trace(request, response, local, remote, waiting, elapse, received);
				} catch (Throwable throwable) {
					Traces.LOGGER.warn(throwable.getMessage(), throwable);
				}
			}
		}
	}
}
