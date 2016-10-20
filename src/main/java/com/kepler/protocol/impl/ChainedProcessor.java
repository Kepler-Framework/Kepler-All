package com.kepler.protocol.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.kepler.extension.Extension;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestProcessor;

/**
 * 
 * @author blueszhang 2016年3月16日
 *
 */
public class ChainedProcessor implements RequestProcessor, Extension {

	private final List<RequestProcessor> processors = new ArrayList<RequestProcessor>();

	private final SortComparator comparator = new SortComparator();

	@Override
	public Request process(Request request) {
		Request each = request;
		if (!this.processors.isEmpty()) {
			for (RequestProcessor processor : this.processors) {
				each = processor.process(each);
			}
		}
		return each;
	}

	public int sort() {
		return Integer.MAX_VALUE;
	}

	@Override
	public ChainedProcessor install(Object instance) {
		this.processors.add(RequestProcessor.class.cast(instance));
		// 每次加载后重新排序
		Collections.sort(this.processors, this.comparator);
		return this;
	}

	@Override
	public Class<?> interested() {
		return RequestProcessor.class;
	}

	/**
	 * Sort 排序
	 * 
	 * @author KimShen
	 *
	 */
	private class SortComparator implements Comparator<RequestProcessor> {

		public int compare(RequestProcessor o1, RequestProcessor o2) {
			int sort = o1.sort() - o2.sort();
			return sort != 0 ? sort : o1.getClass().getName().compareTo(o2.getClass().getName());
		}
	}
}
