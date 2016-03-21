package com.kepler.trace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.extension.Extension;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;

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
	public void trace(Request request, Response response, String local, String remote, long waiting, long elapse, long receivedTime) {
		for (Trace each : this.traces) {
			try {
				each.trace(request, response, local, remote, waiting, elapse, receivedTime);
			} catch (Throwable throwable) {
				Traces.LOGGER.warn(throwable.getMessage(), throwable);
			}
		}
	}
}
