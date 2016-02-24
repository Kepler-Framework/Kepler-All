package com.kepler.header.impl;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.kepler.extension.Extension;
import com.kepler.header.Headers;
import com.kepler.header.HeadersProcessor;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月14日
 */
public class ChainedProcessor implements HeadersProcessor, Extension {

	// sort越小排序越靠前, 如果相同就使用ClassName排序
	private final Set<HeadersProcessor> processors = new TreeSet<HeadersProcessor>(new Comparator<HeadersProcessor>() {
		public int compare(HeadersProcessor o1, HeadersProcessor o2) {
			int sort = o1.sort() - o2.sort();
			return sort != 0 ? sort : o1.getClass().getName().compareTo(o2.getClass().getName());
		}
	});

	@Override
	// 将本次迭代返回Headers作为下次迭代参数Headers
	public Headers process(Service service, Headers headers) {
		Headers each = headers;
		for (HeadersProcessor processor : this.processors) {
			each = processor.process(service, each);
		}
		return each;
	}

	public int sort() {
		return Integer.MAX_VALUE;
	}

	@Override
	public ChainedProcessor install(Object instance) {
		this.processors.add(HeadersProcessor.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return HeadersProcessor.class;
	}
}
