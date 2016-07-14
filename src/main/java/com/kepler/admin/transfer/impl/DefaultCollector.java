package com.kepler.admin.transfer.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import com.kepler.ack.Ack;
import com.kepler.admin.transfer.Collector;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月22日
 */
public class DefaultCollector implements Collector, Imported {

	/**
	 * 当前, 切换, 清理
	 */
	private final MultiKeyMap[] transfers = new MultiKeyMap[] { new MultiKeyMap(), new MultiKeyMap(), new MultiKeyMap() };

	/**
	 * Start from 1
	 */
	private final AtomicInteger indexes = new AtomicInteger(1);

	@Override
	public void subscribe(Service service) throws Exception {
		for (int index = 0; index < this.transfers.length; index++) {
			// 为3个状态同时初始化
			this.methods(service, index);
		}
	}

	private void methods(Service service, int index) throws Exception {
		// 获取所有Method并初始化DefaultTransfers
		for (Method method : Service.clazz(service).getMethods()) {
			this.transfers[index].put(service, method.getName(), new DefaultTransfers(service, method.getName()));
		}
	}

	public Transfer peek(Ack ack) {
		return DefaultTransfers.class.cast(this.curr().get(ack.request().service(), ack.request().method())).get(ack.local(), ack.target());
	}

	@Override
	public Transfer collect(Ack ack) {
		return DefaultTransfers.class.cast(this.curr().get(ack.request().service(), ack.request().method())).put(ack.local(), ack.target(), ack.status(), ack.elapse());
	}

	@SuppressWarnings("unchecked")
	public Collection<Transfers> transfers() {
		return this.exchange().values();
	}

	/**
	 * 清理下次待使用所有DefaultTransfers
	 * 
	 * @return
	 */
	private DefaultCollector clear() {
		for (Object each : DefaultCollector.this.next().values()) {
			DefaultTransfers.class.cast(each).clear();
		}
		return this;
	}

	private MultiKeyMap prev() {
		return this.index(-1);
	}

	private MultiKeyMap curr() {
		return this.index(0);
	}

	private MultiKeyMap next() {
		return this.index(1);
	}

	private MultiKeyMap index(int index) {
		return this.transfers[((this.indexes.get() + index) & Byte.MAX_VALUE) % this.transfers.length];
	}

	private MultiKeyMap exchange() {
		this.indexes.incrementAndGet();
		return this.clear().prev();
	}
}
