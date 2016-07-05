package com.kepler.transaction.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.transaction.Guid;
import com.kepler.transaction.Invoker;
import com.kepler.transaction.Persistent;
import com.kepler.transaction.Request;
import com.kepler.transaction.Transcation;

/**
 * @author KimShen
 *
 */
public class DefaultTranscation implements Transcation, ApplicationContextAware {

	/**
	 * 回滚延迟加权
	 */
	private static final int INTERVAL_ADJUST = PropertiesUtils.get(DefaultTranscation.class.getName().toLowerCase() + ".interval_adjust", 100);

	/**
	 * 回滚延迟加权上限
	 */
	private static final int INTERVAL_MAX = PropertiesUtils.get(DefaultTranscation.class.getName().toLowerCase() + ".interval_max", 10);

	/**
	 * 回滚队列上限
	 */
	private static final int DELAY_MAX = PropertiesUtils.get(DefaultTranscation.class.getName().toLowerCase() + ".delay_max", Integer.MAX_VALUE);

	/**
	 * 回滚队列延迟
	 */
	private static final int DELAY_INTERVAL = PropertiesUtils.get(DefaultTranscation.class.getName().toLowerCase() + ".delay_interval", 500);

	/**
	 * 回滚线程数量
	 */
	private static final int THREAD = PropertiesUtils.get(DefaultTranscation.class.getName().toLowerCase() + ".thread", 1);

	private static final Log LOGGER = LogFactory.getLog(DefaultTranscation.class);

	/**
	 * 内置用于回滚延迟队列
	 */
	private final BlockingQueue<DelayRollback> queue = new DelayQueue<DelayRollback>();

	private final AtomicInteger threshold = new AtomicInteger();

	private final AtomicBoolean shutdown = new AtomicBoolean();

	private final ThreadPoolExecutor executor;

	/**
	 * 用于持久化事务请求
	 */
	private final Persistent persistent;

	private ApplicationContext context;

	public DefaultTranscation(ThreadPoolExecutor executor, Persistent persistent) {
		super();
		this.persistent = persistent;
		this.executor = executor;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public void init() {
		// 用于延迟处理
		for (int index = 0; index < DefaultTranscation.THREAD; index++) {
			this.executor.execute(new Rollback());
		}
		// 用于任务恢复
		this.executor.execute(new Restore());
	}

	public void destroy() {
		this.shutdown.set(true);
	}

	public Object commit(Request request, Invoker invoker) throws Exception {
		try {
			// 追加Guid
			Guid.set(request.uuid());
			// 持久事务
			this.persistent.persist(request);
			// 执行事务
			Object response = invoker.invoke(request.uuid(), request.args());
			// 释放事务
			this.persistent.release(request.uuid());
			return response;
		} catch (Exception e) {
			DefaultTranscation.LOGGER.error(e.getMessage(), e);
			// 执行失败则回滚事务
			this.rollback(request);
			throw e;
		} finally {
			// 释放Guid
			Guid.release();
		}
	}

	/**
	 * 是否允许提交至延迟回滚队列
	 * 
	 * @param uuid 用于Log
	 * @return
	 */
	private boolean allowed(String uuid) {
		boolean allowed = this.threshold.get() <= DefaultTranscation.DELAY_MAX;
		// 如果拒绝加入延迟队列则记录日志
		if (!allowed) {
			DefaultTranscation.LOGGER.warn("Delay queue not allowed this request: " + uuid + " ... ");
		}
		return allowed;
	}

	/**
	 * 加入回滚任务队列
	 * 
	 * @param request
	 */
	private void rollback(Request request) {
		try {
			// 如果延迟队列允许处理该任务
			if (this.allowed(request.uuid())) {
				// 增加阈值计数
				this.threshold.incrementAndGet();
				// 对于失败的事务推送至延迟回滚队列
				this.queue.put(new DelayRollback(request));
			}
		} catch (Throwable e) {
			// 如无法推送至延迟回滚队列则仅下次重启时才会读取持久化事务进行回滚
			DefaultTranscation.LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 延迟回滚任务
	 * 
	 * @author KimShen
	 *
	 */
	private class DelayRollback implements Delayed {

		private final Request request;

		/**
		 * 当前时间 + delay时间
		 */
		private long deadline;

		/**
		 * 重试次数
		 */
		private int tries;

		private DelayRollback(Request request) {
			super();
			this.request = request;
			//初始化
			this.prepare();
		}

		/**
		 * 延迟回滚任务准备, 包括重置延迟时间和增加内置请求次数日志
		 * 
		 * @return
		 */
		public DelayRollback prepare() {
			// 计算重试次数相关的额外延迟
			long extend = Math.min(this.tries, DefaultTranscation.INTERVAL_MAX) * DefaultTranscation.INTERVAL_ADJUST;
			DefaultTranscation.LOGGER.info("Rollback for " + this.request.location() + ", retry " + this.tries + " times and extend " + extend + " ms ... ");
			this.deadline = extend + TimeUnit.MILLISECONDS.convert(DefaultTranscation.DELAY_INTERVAL, TimeUnit.MILLISECONDS) + System.currentTimeMillis();
			this.tries++;
			return this;
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		public int compareTo(Delayed o) {
			return this.getDelay(TimeUnit.SECONDS) >= o.getDelay(TimeUnit.SECONDS) ? 1 : -1;
		}

		/**
		 * 执行回滚
		 * 
		 * @return 如果回滚成功则返回True
		 */
		public boolean rollback() {
			try {
				Guid.set(this.request.uuid());
				MethodUtils.invokeMethod(DefaultTranscation.this.context.getBean(this.request.location().clazz()), this.request.location().method(), this.request.args());
				// 回滚成功并且删除持久化文件才表示成功
				DefaultTranscation.this.persistent.release(this.request.uuid());
				return true;
			} catch (Throwable e) {
				// 回滚失败
				DefaultTranscation.LOGGER.error(e.getMessage(), e);
				return false;
			} finally {
				Guid.release();
			}
		}

		/**
		 * 获取UUID
		 * 
		 * @return
		 */
		public String uuid() {
			return this.request.uuid();
		}
	}

	/**
	 * 延迟回滚执行器
	 * 
	 * @author KimShen
	 *
	 */
	private class Rollback implements Runnable {

		@Override
		public void run() {
			while (!DefaultTranscation.this.shutdown.get()) {
				try {
					// 如果延迟回滚任务再次失败则重置后推送至等待队列
					DelayRollback rollback = DefaultTranscation.this.queue.take();
					// 减少阈值计数
					DefaultTranscation.this.threshold.decrementAndGet();
					// 尝试回滚, 如果失败则计算是否允许加入延迟回滚队列. 如果允许则放入延迟回滚队列
					if (!rollback.rollback() && DefaultTranscation.this.allowed(rollback.uuid())) {
						// 增加阈值计数
						DefaultTranscation.this.threshold.incrementAndGet();
						DefaultTranscation.this.queue.offer(rollback.prepare());
					}
				} catch (Throwable e) {
					DefaultTranscation.LOGGER.error(e.getMessage(), e);
				}
			}
			DefaultTranscation.LOGGER.warn(this.getClass() + " shutdown on thread (" + Thread.currentThread().getId() + ")");
		}
	}

	/**
	 * 任务恢复执行器
	 * 
	 * @author KimShen
	 *
	 */
	private class Restore implements Runnable {

		@Override
		public void run() {
			for (Request request : DefaultTranscation.this.persistent.list()) {
				try {
					DefaultTranscation.this.queue.put(new DelayRollback(request));
				} catch (Throwable e) {
					DefaultTranscation.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
