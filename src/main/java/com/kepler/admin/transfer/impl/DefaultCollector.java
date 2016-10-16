package com.kepler.admin.transfer.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log LOGGER = LogFactory.getLog(DefaultCollector.class);

	/**
	 * 当前, 切换, 清理
	 */
	private final MultiKeyMap[] transfers = new MultiKeyMap[] { new MultiKeyMap(), new MultiKeyMap(), new MultiKeyMap() };

	/**
	 * Start from 1
	 */
	private final AtomicInteger indexes = new AtomicInteger(1);

	/**
	 * 指定服务, 指定方法加载Transfers
	 * 
	 * @param service 服务
	 * @param method 方法名称
	 * @return Current Transfers
	 */
	private Transfers methods(Service service, String method) {
		// 泛化加载时的线程安全
		synchronized (this) {
			// Guard case, 如果已存在则返回
			Transfers current = Transfers.class.cast(this.curr().get(service, method));
			if (current != null) {
				return current;
			}
			// 初始化并返回Current
			for (int index = 0; index < this.transfers.length; index++) {
				this.transfers[index].put(service, method, new DefaultTransfers(service, method));
			}
			return Transfers.class.cast(this.curr().get(service, method));
		}
	}

	@Override
	public void subscribe(Service service) throws Exception {
		try {
			// 获取所有Method并初始化DefaultTransfers
			for (Method method : Service.clazz(service).getMethods()) {
				this.methods(service, method.getName());
			}
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			DefaultCollector.LOGGER.info("Class not found: " + service);
		}
	}

	/**
	 * 从ACK定位Transfers
	 * 
	 * @param ack
	 * @return
	 */
	private Transfers get(Ack ack) {
		Transfers transfers = Transfers.class.cast(this.curr().get(ack.request().service(), ack.request().method()));
		// 如果未加载到Transfers(如Generic)则加载后尝试重新获取
		return transfers == null ? this.methods(ack.request().service(), ack.request().method()) : transfers;
	}

	public Transfer peek(Ack ack) {
		return this.get(ack).get(ack.local(), ack.target());
	}

	@Override
	public Transfer collect(Ack ack) {
		// 获取Transfers并判断是否为Generic
		return this.get(ack).put(ack.local(), ack.target(), ack.status(), ack.elapse());
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
		for (Object each : this.next().values()) {
			DefaultTransfers.class.cast(each).clear();
		}
		return this;
	}

	/**
	 * 重置下次待使用所有DefaultTransfers
	 * 
	 * @return
	 */
	private DefaultCollector reset() {
		for (Object each : this.next().values()) {
			DefaultTransfers.class.cast(each).reset();
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
		// 重置待使用缓存区
		this.reset();
		// 切换待使用缓存区为当前缓存区
		this.indexes.incrementAndGet();
		// 清理下次使用缓存区并返回上次使用缓存区
		return this.clear().prev();
	}
}
