package com.kepler.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;

/**
 * 线程池代理, 用于在多线程间传递Headers
 * 
 * @author KimShen
 *
 */
public class ThreadFactoryDelegate implements Executor{

	private final ThreadPoolExecutor executor;

	private final HeadersContext context;

	public ThreadFactoryDelegate(ThreadPoolExecutor executor, HeadersContext context) {
		super();
		this.executor = executor;
		this.context = context;
	}

	public void execute(Runnable command) {
		this.executor.execute(new HeaderRunnable(command));
	}

	public <T> Future<T> submit(Callable<T> task) {
		return this.executor.submit(new HeaderCallable<T>(task));
	}

	public Future<?> submit(Runnable task) {
		return this.executor.submit(new HeaderRunnable(task));
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return this.executor.submit(new HeaderRunnable(task), result);
	}

	private class HeaderRunnable implements Runnable {

		// 复制当期线程Headers
		private final Headers header = new LazyHeaders(ThreadFactoryDelegate.this.context.get().get());

		private final Runnable runnable;

		private HeaderRunnable(Runnable runnable) {
			super();
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				// 恢复Headers
				ThreadFactoryDelegate.this.context.set(this.header);
				this.runnable.run();
			} finally {
				// 释放Headers
				ThreadFactoryDelegate.this.context.release();
			}
		}
	}

	private class HeaderCallable<T> implements Callable<T> {

		// 复制当期线程Headers
		private final Headers header = new LazyHeaders(ThreadFactoryDelegate.this.context.get().get());

		private final Callable<T> callable;

		public HeaderCallable(Callable<T> callable) {
			super();
			this.callable = callable;
		}

		@Override
		public T call() throws Exception {
			try {
				// 恢复Headers
				ThreadFactoryDelegate.this.context.set(this.header);
				return this.callable.call();
			} finally {
				// 释放Headers
				ThreadFactoryDelegate.this.context.release();
			}
		}
	}
}
