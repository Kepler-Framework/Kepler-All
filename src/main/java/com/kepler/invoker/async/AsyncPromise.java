package com.kepler.invoker.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kim
 *
 * 2016年2月16日
 */
public class AsyncPromise {

	private final ThreadPoolExecutor threads;

	public AsyncPromise(ThreadPoolExecutor threads) {
		super();
		this.threads = threads;
	}

	@SafeVarargs
	final public void promise(AsyncCallback callback, Future<Object>... futures) {
		this.threads.execute(new FutureLaunch(callback, futures));
	}

	@SafeVarargs
	final public void promise(int timeout, AsyncCallback callback, Future<Object>... futures) {
		this.threads.execute(new FutureLaunch(timeout, callback, futures));
	}

	@SafeVarargs
	final public static void promise(AsyncCallback callback, Executor executor, Future<Object>... futures) {
		executor.execute(new FutureLaunch(callback, futures));
	}

	@SafeVarargs
	final public static void promise(int timeout, AsyncCallback callback, Executor executor, Future<Object>... futures) {
		executor.execute(new FutureLaunch(timeout, callback, futures));
	}

	/**
	 * 
	 * @author kim
	 *
	 * 2016年2月16日
	 */
	private static class FutureLaunch implements Runnable {

		/**
		 * 待处理Future
		 */
		private final Future<Object>[] futures;

		private final AsyncCallback callback;

		/**
		 * 实际Future结果
		 */
		private final Object[] args;

		private final int timeout;

		@SafeVarargs
		private FutureLaunch(AsyncCallback callback, Future<Object>... futures) {
			this(Integer.MAX_VALUE, callback, futures);
		}

		@SafeVarargs
		private FutureLaunch(int timeout, AsyncCallback callback, Future<Object>... futures) {
			this.args = new Object[futures.length];
			this.callback = callback;
			this.timeout = timeout;
			this.futures = futures;
		}

		public void run() {
			try {
				// 准备数据, 回调
				this.callback.prepare(this.get4args(this.args, this.timeout)).run();
			} catch (Throwable throwable) {
				// 异常处理
				this.callback.throwable(throwable);
			} finally {
				this.release();
			}
		}

		private void release() {
			// 尝试取消剩余任务
			for (Future<Object> each : this.futures) {
				each.cancel(true);
			}
		}

		private Object[] get4args(Object[] args, int timeout) throws Throwable {
			// 准备Future数据
			for (int index = 0; index < args.length; index++) {
				args[index] = this.futures[index].get(timeout, TimeUnit.MILLISECONDS);
			}
			return args;
		}
	}
}
