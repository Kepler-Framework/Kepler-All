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
import com.kepler.transaction.Transcation;
import com.kepler.transaction.TranscationContext;
import com.kepler.transaction.TranscationPersistent;
import com.kepler.transaction.TranscationRequest;

/**
 * @author KimShen
 *
 */
public class DefaultContext implements TranscationContext, ApplicationContextAware {

	/**
	 * 回滚延迟加权
	 */
	private static final int INTERVAL_ADJUST = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".interval_adjust", 100);

	/**
	 * 回滚延迟加权上限
	 */
	private static final int INTERVAL_MAX = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".interval_max", 10);

	/**
	 * 回滚队列上限
	 */
	private static final int DELAY_MAX = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".delay_max", Integer.MAX_VALUE);

	/**
	 * 回滚队列延迟
	 */
	private static final int DELAY_INTERVAL = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".delay_interval", 500);

	/**
	 * 回滚线程数量
	 */
	private static final int THREAD = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".thread", 1);

	private static final Log LOGGER = LogFactory.getLog(DefaultContext.class);

	/**
	 * 内置用于回滚延迟队列
	 */
	private final BlockingQueue<DelayRollback> queue = new DelayQueue<DelayRollback>();

	private final AtomicInteger threshold = new AtomicInteger();

	private final AtomicBoolean shutdown = new AtomicBoolean();

	/**
	 * 用于持久化事务请求
	 */
	private final TranscationPersistent persistent;

	private final ThreadPoolExecutor executor;

	private ApplicationContext context;

	public DefaultContext(ThreadPoolExecutor executor, TranscationPersistent persistent) {
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
		for (int index = 0; index < DefaultContext.THREAD; index++) {
			this.executor.execute(new Rollback());
		}
		// 用于任务恢复
		this.executor.execute(new Restore());
	}

	public void destroy() {
		this.shutdown.set(true);
	}

	public boolean commit(TranscationRequest request) {
		// 持久化成功 -> 同步执行成功则提交成功, 否则回滚
		return this.persistent.persist(request) ? this.invoke(request) : false;
	}

	/**
	 * 执行事务
	 * 
	 * @param request 
	 * @return
	 */
	private boolean invoke(TranscationRequest request) {
		try {
			// 执行事务
			this.context.getBean(request.main()).transcation(request.uuid(), request.args());
			// 执行成功释放事务
			this.persistent.release(request.uuid());
			return true;
		} catch (Throwable e) {
			DefaultContext.LOGGER.error(e.getMessage(), e);
			// 执行失败则回滚事务
			this.rollback(request);
			return false;
		}
	}

	/**
	 * 是否允许提交至延迟回滚队列
	 * 
	 * @param uuid 用于Log
	 * @return
	 */
	private boolean allowed(String uuid) {
		boolean allowed = this.threshold.get() <= DefaultContext.DELAY_MAX;
		// 如果拒绝加入延迟队列则记录日志
		if (!allowed) {
			DefaultContext.LOGGER.warn("Delay queue not allowed this request: " + uuid + " ... ");
		}
		return allowed;
	}

	/**
	 * 加入回滚任务队列
	 * 
	 * @param request
	 */
	private void rollback(TranscationRequest request) {
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
			DefaultContext.LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 延迟回滚任务
	 * 
	 * @author KimShen
	 *
	 */
	private class DelayRollback implements Delayed {

		/**
		 * 回滚用事务
		 */
		private final Transcation rollback;

		/**
		 * 回滚参数
		 */
		private final Object[] args;

		/**
		 * 事务编号
		 */
		private final String uuid;

		/**
		 * 当前时间 + delay时间
		 */
		private long deadline;

		/**
		 * 重试次数
		 */
		private int tries;

		private DelayRollback(TranscationRequest request) {
			super();
			this.rollback = DefaultContext.this.context.getBean(request.rollback());
			this.args = request.args();
			this.uuid = request.uuid();
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
			long extend = Math.min(this.tries, DefaultContext.INTERVAL_MAX) * DefaultContext.INTERVAL_ADJUST;
			DefaultContext.LOGGER.info("Rollback for " + this.rollback.getClass() + " retry " + this.tries + " times and extend " + extend + " ms ... ");
			this.deadline = extend + TimeUnit.MILLISECONDS.convert(DefaultContext.DELAY_INTERVAL, TimeUnit.MILLISECONDS) + System.currentTimeMillis();
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
				this.rollback.transcation(this.uuid, this.args);
				// 回滚成功并且删除持久化文件才表示成功
				DefaultContext.this.persistent.release(this.uuid);
				return true;
			} catch (Throwable e) {
				// 回滚失败
				DefaultContext.LOGGER.error(e.getMessage(), e);
				return false;
			}
		}

		/**
		 * 获取UUID
		 * 
		 * @return
		 */
		public String uuid() {
			return this.uuid;
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
			while (!DefaultContext.this.shutdown.get()) {
				try {
					// 如果延迟回滚任务再次失败则重置后推送至等待队列
					DelayRollback rollback = DefaultContext.this.queue.take();
					// 减少阈值计数
					DefaultContext.this.threshold.decrementAndGet();
					// 尝试回滚, 如果失败则计算是否允许加入延迟回滚队列. 如果允许则放入延迟回滚队列
					if (!rollback.rollback() && DefaultContext.this.allowed(rollback.uuid())) {
						// 增加阈值计数
						DefaultContext.this.threshold.incrementAndGet();
						DefaultContext.this.queue.offer(rollback.prepare());
					}
				} catch (Throwable e) {
					DefaultContext.LOGGER.error(e.getMessage(), e);
				}
			}
			DefaultContext.LOGGER.warn(this.getClass() + " shutdown on thread (" + Thread.currentThread().getId() + ")");
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
			for (TranscationRequest request : DefaultContext.this.persistent.list()) {
				try {
					DefaultContext.this.queue.put(new DelayRollback(request));
				} catch (Throwable e) {
					DefaultContext.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
