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
import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.transaction.Guid;
import com.kepler.transaction.Invoker;
import com.kepler.transaction.Persistent;
import com.kepler.transaction.Request;
import com.kepler.transaction.Transaction;

/**
 * @author KimShen
 *
 */
public class DefaultTransaction implements Transaction, ApplicationContextAware {

	/**
	 * 回滚延迟加权
	 */
	private static final int INTERVAL_ADJUST = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".interval_adjust", 100);

	/**
	 * 回滚延迟加权上限
	 */
	private static final int INTERVAL_MAX = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".interval_max", 10);

	/**
	 * 回滚尝试上限
	 */
	private static final int DELAY_TIMES = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".delay_times", Integer.MAX_VALUE);

	/**
	 * 回滚队列上限
	 */
	private static final int DELAY_MAX = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".delay_max", Integer.MAX_VALUE);

	/**
	 * 回滚队列延迟
	 */
	private static final int DELAY_INTERVAL = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".delay_interval", 500);

	/**
	 * 是否激活事务回滚
	 */
	private static final boolean ACTIVED = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".actived", false);

	/**
	 * 回滚线程数量
	 */
	private static final int THREAD = PropertiesUtils.get(DefaultTransaction.class.getName().toLowerCase() + ".thread", 1);

	private static final Log LOGGER = LogFactory.getLog(DefaultTransaction.class);

	/**
	 * 内置用于回滚延迟队列
	 */
	private final BlockingQueue<DelayRollback> queue = new DelayQueue<DelayRollback>();

	private final AtomicInteger threshold = new AtomicInteger();

	private final AtomicBoolean shutdown = new AtomicBoolean();

	private final ThreadPoolExecutor executor;

	private final HeadersContext headers;

	/**
	 * 用于持久化事务请求
	 */
	private final Persistent persistent;

	private ApplicationContext context;

	public DefaultTransaction(ThreadPoolExecutor executor, HeadersContext headers, Persistent persistent) {
		super();
		this.persistent = persistent;
		this.executor = executor;
		this.headers = headers;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public void init() {
		if (DefaultTransaction.ACTIVED) {
			DefaultTransaction.LOGGER.info("Init rollback threads: " + DefaultTransaction.THREAD + " ... ");
			// 用于延迟处理
			for (int index = 0; index < DefaultTransaction.THREAD; index++) {
				this.executor.execute(new Rollback());
			}
			// 用于任务恢复
			this.executor.execute(new Restore());
		}
	}

	public void destroy() {
		this.shutdown.set(true);
	}

	public Object commit(Request request, Invoker invoker) throws Exception {
		try {
			// 追加Guid
			Guid.set(request.uuid());
			// 复制Header并持久事务
			this.persistent.persist(request.headers(new LazyHeaders(this.headers.get())));
			// 执行事务
			Object response = invoker.invoke(request.uuid(), request.args());
			// 释放事务
			this.persistent.release(request.uuid());
			return response;
		} catch (Exception e) {
			DefaultTransaction.LOGGER.error(e.getMessage(), e);
			// 如果激活回滚则执行失败则回滚事务
			if (DefaultTransaction.ACTIVED) {
				this.rollback(request);
			}
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
		boolean allowed = this.threshold.get() <= DefaultTransaction.DELAY_MAX;
		// 如果拒绝加入延迟队列则记录日志
		if (!allowed) {
			DefaultTransaction.LOGGER.warn("Delay queue not allowed this request: " + uuid + " ... ");
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
			DefaultTransaction.LOGGER.error(e.getMessage(), e);
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
			long extend = Math.min(this.tries, DefaultTransaction.INTERVAL_MAX) * DefaultTransaction.INTERVAL_ADJUST;
			DefaultTransaction.LOGGER.info("Rollback for " + this.request.location() + ", retry " + this.tries + " times and extend " + extend + " ms ... ");
			this.deadline = extend + TimeUnit.MILLISECONDS.convert(DefaultTransaction.DELAY_INTERVAL, TimeUnit.MILLISECONDS) + System.currentTimeMillis();
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
				// 重置Headers
				DefaultTransaction.this.headers.set(this.request.headers());
				Guid.set(this.request.uuid());
				Object service = DefaultTransaction.this.context.getBean(this.request.location().clazz());
				// 如果为Invoker则执行Invoker, 否则执行特定Class
				if (Invoker.class.isAssignableFrom(service.getClass())) {
					Invoker.class.cast(service).invoke(this.request.uuid(), this.request.args());
				} else {
					MethodUtils.invokeMethod(service, this.request.location().method(), this.request.args());
				}
				// 回滚成功并且删除持久化文件才表示成功
				DefaultTransaction.this.persistent.release(this.request.uuid());
				return true;
			} catch (Throwable e) {
				// 回滚失败
				DefaultTransaction.LOGGER.error("UUID: " + this.request.uuid() + " message: " + e.getMessage(), e);
				return false;
			} finally {
				// 重置Headers
				DefaultTransaction.this.headers.reset();
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

		/**
		 * 是否终止回滚
		 * 
		 * @return
		 */
		public boolean terminate() {
			// 尝试次数大于上线则终止
			boolean terminate = this.tries >= DefaultTransaction.DELAY_TIMES;
			if (terminate) {
				DefaultTransaction.LOGGER.warn("Request: " + this.request.uuid() + " will be terminated ... ");
			}
			return terminate;
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
			while (!DefaultTransaction.this.shutdown.get()) {
				try {
					// 如果延迟回滚任务再次失败则重置后推送至等待队列
					DelayRollback rollback = DefaultTransaction.this.queue.take();
					// 减少阈值计数
					DefaultTransaction.this.threshold.decrementAndGet();
					// 尝试回滚, 如果失败并且允许允许继续尝试,则计算是否允许加入延迟回滚队列. 如果允许则放入延迟回滚队列
					if (!rollback.rollback() && !rollback.terminate() && DefaultTransaction.this.allowed(rollback.uuid())) {
						// 增加阈值计数
						DefaultTransaction.this.threshold.incrementAndGet();
						DefaultTransaction.this.queue.offer(rollback.prepare());
					}
				} catch (Throwable e) {
					DefaultTransaction.LOGGER.error(e.getMessage(), e);
				}
			}
			DefaultTransaction.LOGGER.warn(this.getClass() + " shutdown on thread (" + Thread.currentThread().getId() + ")");
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
			for (Request request : DefaultTransaction.this.persistent.list()) {
				try {
					DefaultTransaction.this.queue.put(new DelayRollback(request));
				} catch (Throwable e) {
					DefaultTransaction.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
