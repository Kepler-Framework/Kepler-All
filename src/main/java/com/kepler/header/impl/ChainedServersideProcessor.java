package com.kepler.header.impl;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.kepler.extension.Extension;
import com.kepler.header.Headers;
import com.kepler.header.ServersideHeadersProcessor;
import com.kepler.service.Service;

/**
 * 
 * @author blueszhang 2016年3月16日
 *
 */
public class ChainedServersideProcessor implements ServersideHeadersProcessor, Extension {

	private final Set<ServersideHeadersProcessor> processors = new TreeSet<ServersideHeadersProcessor>(
			new Comparator<ServersideHeadersProcessor>() {
				public int compare(ServersideHeadersProcessor o1, ServersideHeadersProcessor o2) {
					int sort = o1.sort() - o2.sort();
					return sort != 0 ? sort : o1.getClass().getName().compareTo(o2.getClass().getName());
				}
			});

	@Override
	public Headers process(Service service, Headers headers) {
		Headers each = headers;
		for (ServersideHeadersProcessor processor : this.processors) {
			each = processor.process(service, each);
		}
		return each;
	}

	public int sort() {
		return Integer.MAX_VALUE;
	}

	@Override
	public ChainedServersideProcessor install(Object instance) {
		this.processors.add(ServersideHeadersProcessor.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return ServersideHeadersProcessor.class;
	}
}
