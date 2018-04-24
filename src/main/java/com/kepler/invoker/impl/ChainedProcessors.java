package com.kepler.invoker.impl;

import java.util.ArrayList;
import java.util.List;

import com.kepler.extension.Extension;
import com.kepler.host.Host;
import com.kepler.invoker.InvokerProcessor;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class ChainedProcessors implements Extension, InvokerProcessor {

	private final List<InvokerProcessor> processor = new ArrayList<InvokerProcessor>();

	@Override
	public Request before(Request request, Host host) {
		Request temp = request;
		for (InvokerProcessor each : this.processor) {
			temp = each.before(temp, host);
		}
		return temp;
	}

	@Override
	public ChainedProcessors install(Object instance) {
		this.processor.add(InvokerProcessor.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return InvokerProcessor.class;
	}
}
