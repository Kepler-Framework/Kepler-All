package com.kepler.admin.transfer.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Ack;
import com.kepler.admin.transfer.Collector;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月22日
 */
public class DefaultCollector implements Runnable, Collector, Imported {

	/**
	 * 队列长度
	 */
	private static final int QUEUE_SIZE = PropertiesUtils.get(DefaultCollector.class.getName().toLowerCase() + ".queue_size", Short.MAX_VALUE);

	private static final int INTERVAL = PropertiesUtils.get(DefaultCollector.class.getName().toLowerCase() + ".interval", 60000);

	private static final Log LOGGER = LogFactory.getLog(DefaultCollector.class);

	/**
	 * 当前, 切换, 清理
	 */
	private final MultiKeyMap[] transfers = new MultiKeyMap[] { new MultiKeyMap(), new MultiKeyMap(), new MultiKeyMap() };

	/**
	 * 等待队列
	 */
	private final BlockingQueue<Ack> acks = new ArrayBlockingQueue<Ack>(DefaultCollector.QUEUE_SIZE);

	private final ThreadPoolExecutor threads;

	volatile private boolean shutdown;

	/**
	 * Start from 1
	 */
	volatile private int indexes;

	public DefaultCollector(ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
		this.shutdown = false;
		this.indexes = 1;
	}

	/**
	 * For Spring
	 */
	public void init() {
		this.threads.execute(this);
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		this.shutdown = true;
	}

	/**
	 * 加载指定服务Transfers
	 * 
	 * @param service 服务
	 * @param method 方法名称
	 * @return Current Transfers
	 */
	private Transfers install(Service service, String method) {
		// 泛化加载时的线程安全
		synchronized (this) {
			// Guard case, 同步检查
			Transfers current = Transfers.class.cast(this.curr().get(service, method));
			if (current != null) {
				return current;
			}
			// 初始化并返回
			for (int index = 0; index < this.transfers.length; index++) {
				this.transfers[index].put(service, method, new DefaultTransfers(service, method));
			}
			return Transfers.class.cast(this.curr().get(service, method));
		}
	}

	@Override
	public void subscribe(Service service) throws Exception {
		try {
			for (Method method : Service.clazz(service).getMethods()) {
				this.install(service, method.getName());
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
		return transfers == null ? this.install(ack.request().service(), ack.request().method()) : transfers;
	}

	public Transfer peek(Ack ack) {
		return this.get(ack).get(ack.local(), ack.remote());
	}

	@Override
	public void collect(Ack ack) {
		if (!this.acks.offer(ack)) {
			// 插入失败提示
			DefaultCollector.LOGGER.warn("Collect ack failed: " + Arrays.toString(ack.request().ack()));
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Transfers> transfers() {
		// 交换缓存区并获取本次收集结果
		return this.exchange().values();
	}

	/**
	 * 清理下次待使用缓存区
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
	 * 重置下次待使用缓存区
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

	/**
	 * 获取指定索引位置缓存区
	 * 
	 * @param index
	 * @return
	 */
	private MultiKeyMap index(int index) {
		return this.transfers[((this.indexes + index) & Byte.MAX_VALUE) % this.transfers.length];
	}

	private MultiKeyMap exchange() {
		// 重置待使用缓存区
		this.reset();
		// 切换待使用缓存区为当前缓存区
		this.indexes++;
		// 清理下次使用缓存区并返回上次使用缓存区
		return this.clear().prev();
	}

	@Override
	public void run() {
		while (!this.shutdown) {
			try {
				Ack ack = this.acks.poll(DefaultCollector.INTERVAL, TimeUnit.MILLISECONDS);
				if (ack != null) {
					this.get(ack).put(ack.local(), ack.remote(), ack.status(), ack.elapse());
				}
			} catch (Throwable e) {
				DefaultCollector.LOGGER.debug(e.getMessage(), e);
			}
		}
		DefaultCollector.LOGGER.warn("Collector shutdown ... ");
	}
}
