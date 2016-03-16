package com.kepler.protocol.impl;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.kepler.extension.Extension;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestProcessor;

/**
 * 
 * @author blueszhang 2016年3月16日
 *
 */
public class ChainedProcessor implements RequestProcessor, Extension {

	private final Set<RequestProcessor> processors = new TreeSet<RequestProcessor>(new Comparator<RequestProcessor>() {
		public int compare(RequestProcessor o1, RequestProcessor o2) {
			int sort = o1.sort() - o2.sort();
			return sort != 0 ? sort : o1.getClass().getName().compareTo(o2.getClass().getName());
		}
	});

	@Override
	public Request process(Request request) {
		Request each = request;
		for (RequestProcessor processor : this.processors) {
			each = processor.process(each);
		}
		return each;
	}

	public int sort() {
		return Integer.MAX_VALUE;
	}

	@Override
	public ChainedProcessor install(Object instance) {
		this.processors.add(RequestProcessor.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return RequestProcessor.class;
	}
}
