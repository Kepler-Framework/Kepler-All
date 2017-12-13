package com.kepler.admin.transfer.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Ack;
import com.kepler.admin.status.impl.StatusTask;
import com.kepler.admin.transfer.Collector;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.impl.TraceContext;
import com.kepler.org.apache.commons.collections.keyvalue.MultiKey;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.service.Imported;
import com.kepler.service.Service;
import com.kepler.trace.TraceCauses;

/**
 * @author kim 2015年7月22日
 */
public class DefaultCollector implements Runnable, Collector, Imported {

	/**
	 * 队列长度
	 */
	private static final int QUEUE_SIZE = PropertiesUtils.get(DefaultCollector.class.getName().toLowerCase() + ".queue_size", Integer.MAX_VALUE);

	private static final int INTERVAL = PropertiesUtils.get(DefaultCollector.class.getName().toLowerCase() + ".interval", 60000);

	private static final Log LOGGER = LogFactory.getLog(DefaultCollector.class);

	/**
	 * 等待队列
	 */
	private final BlockingQueue<Ack> acks = new LinkedBlockingQueue<Ack>(DefaultCollector.QUEUE_SIZE);

	/**
	 * 当前, 切换, 清理
	 */
	volatile private MultiKeyMap[] transfers = new MultiKeyMap[] { new MultiKeyMap(), new MultiKeyMap(), new MultiKeyMap() };

	volatile private boolean shutdown = false;

	/**
	 * Start from 1
	 */
	volatile private int indexes = 1;

	private final ThreadPoolExecutor threads;

	private final TraceCauses trace;

	public DefaultCollector(TraceCauses trace, ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
		this.trace = trace;
	}

	/**
	 * For Spring
	 */
	public void init() {
		// 单线程操作
		this.threads.execute(this);
	}

	/**
	 * For Spring
	 */
	public void destroy() {
		this.shutdown = true;
	}

	private void subscribe(Service service, String... methods) throws Exception {
		synchronized (this) {
			MultiKeyMap[] transfers = new MultiKeyMap[this.transfers.length];
			for (int index = 0; index < transfers.length; index++) {
				// Create On Copy
				(transfers[index] = new MultiKeyMap()).putAll(this.transfers[index]);
				for (String method : methods) {
					// Double Check
					if (transfers[index].containsKey(service, method)) {
						continue;
					}
					transfers[index].put(service, method, new DefaultTransfers(this.trace, service, method));
				}
			}
			this.transfers = transfers;
		}
	}

	@Override
	public void subscribe(Service service) throws Exception {
		try {
			List<String> methods = new ArrayList<String>();
			for (Method each : Service.clazz(service).getMethods()) {
				methods.add(each.getName());
			}
			this.subscribe(service, methods.toArray(new String[] {}));
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			DefaultCollector.LOGGER.info("Class not found: " + service);
		}
	}

	public void unsubscribe(Service service) throws Exception {
		MultiKeyMap[] transfers = new MultiKeyMap[this.transfers.length];
		for (int index = 0; index < transfers.length; index++) {
			transfers[index] = new MultiKeyMap();
			for (Object each : this.transfers[index].keySet()) {
				MultiKey key = MultiKey.class.cast(each);
				// 如果Key不属于指定Service则追加
				if (!key.getKey(0).equals(service)) {
					transfers[index].put(key.getKey(0), key.getKey(1), this.transfers[index].get(key.getKey(0), key.getKey(1)));
				}
			}
		}
		this.transfers = transfers;
	}

	public Transfer peek(Ack ack) {
		Transfers transfers = Transfers.class.cast(this.curr().get(ack.request().service(), ack.request().method()));
		Transfer transfer = transfers.get(ack.local(), ack.remote());
		if (transfer == null) {
			DefaultCollector.LOGGER.warn("Empty transfer for " + ack.request().service() + "[method=" + ack.request().method() + "][local=" + ack.local() + "][remote=" + ack.remote() + "]");
		}
		return transfer;
	}

	@Override
	public void collect(Ack ack) {
		if (StatusTask.ENABLED && !this.acks.offer(ack)) {
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
				// Guard case, 无需处理
				if (ack == null) {
					continue;
				}
				// 绑定ACK Trace
				TraceContext.getTraceOnCreate(ack.trace());
				Transfers transfers = Transfers.class.cast(this.curr().get(ack.request().service(), ack.request().method()));
				if (transfers == null) {
					// 泛化请求
					this.subscribe(ack.request().service(), ack.request().method());
					transfers = Transfers.class.cast(this.curr().get(ack.request().service(), ack.request().method()));
					DefaultCollector.LOGGER.info("Create Transfers for [service =" + ack.request().service() + "][method=" + ack.request().method() + "]");
				}
				transfers.put(ack.local(), ack.remote(), ack.status(), ack.elapse());
			} catch (Throwable e) {
				DefaultCollector.LOGGER.debug(e.getMessage(), e);
			} finally {
				// 释放ACK Trace
				TraceContext.release();
			}
		}
		DefaultCollector.LOGGER.warn("Collector shutdown ... ");
	}
}
