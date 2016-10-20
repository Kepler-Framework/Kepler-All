package com.kepler.mock.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.kepler.extension.Extension;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.service.Service;

/**
 * MockerContext链
 * 
 * @author longyaokun
 * @date 2016年8月17日
 */
public class ChainedMockerContext implements Extension, MockerContext {

	private List<MockerContext> context = new ArrayList<MockerContext>();

	private final DefaultComparator comparator = new DefaultComparator();

	@Override
	public Extension install(Object instance) {
		MockerContext mockerContext = MockerContext.class.cast(instance);
		this.context.add(mockerContext);
		// 每次加载触发重排序
		Collections.sort(this.context, this.comparator);
		return this;
	}

	@Override
	public Class<?> interested() {
		return MockerContext.class;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Mocker get(Service service) {
		if (!this.context.isEmpty()) {
			for (MockerContext each : this.context) {
				Mocker mocker = each.get(service);
				if (mocker != null) {
					return mocker;
				}
			}
		}
		return null;
	}

	private class DefaultComparator implements Comparator<MockerContext> {

		@Override
		public int compare(MockerContext o1, MockerContext o2) {
			return o1.getOrder() - o2.getOrder();
		}
	}
}
