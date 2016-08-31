package com.kepler.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.kepler.header.impl.TraceContext;

/**
 * 线程池代理, 用于在多线程间传递Headers
 * 
 * @author KimShen
 *
 */
public class ThreadFactoryDelegate implements ExecutorService {

	private static final List<Runnable> EMPTY = Collections.unmodifiableList(new ArrayList<Runnable>());

	private static final Log LOGGER = LogFactory.getLog(ThreadFactoryDelegate.class);

	/**
	 * 代理Executor
	 */
	private final ThreadPoolExecutor executor;

	private final HeadersContext context;

	public ThreadFactoryDelegate(ThreadPoolExecutor executor, HeadersContext context) {
		super();
		this.executor = executor;
		this.context = context;
	}

	private void warning() {
		ThreadFactoryDelegate.LOGGER.warn("ThreadFactoryDelegate could not support this function ... ");
	}

	/**
	 * 普通Callable包装为Header
	 * 
	 * @param tasks
	 * @return
	 */
	private <T> List<Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
		List<Callable<T>> headers = new ArrayList<Callable<T>>();
		for (Callable<T> each : tasks) {
			headers.add(new HeaderCallable<T>(each));
		}
		return headers;
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

	@Override
	public void shutdown() {
		this.warning();
	}

	@Override
	public List<Runnable> shutdownNow() {
		this.warning();
		return ThreadFactoryDelegate.EMPTY;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.executor.invokeAll(this.wrap(tasks));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.invokeAll(this.wrap(tasks), timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.executor.invokeAny(this.wrap(tasks));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.executor.invokeAny(this.wrap(tasks), timeout, unit);
	}

	private class HeaderRunnable implements Runnable {

		// 复制当期线程Headers
		private final Headers header = new LazyHeaders(ThreadFactoryDelegate.this.context.get());

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
				// 开启Trace
				TraceContext.trace();
				// 代理执行
				this.runnable.run();
			} finally {
				// 释放Headers
				ThreadFactoryDelegate.this.context.release();
			}
		}
	}

	private class HeaderCallable<T> implements Callable<T> {

		// 复制当期线程Headers
		private final Headers header = new LazyHeaders(ThreadFactoryDelegate.this.context.get());

		private final Callable<T> callable;

		private HeaderCallable(Callable<T> callable) {
			super();
			this.callable = callable;
		}

		@Override
		public T call() throws Exception {
			try {
				// 恢复Headers
				ThreadFactoryDelegate.this.context.set(this.header);
				// 开启Trace
				TraceContext.trace();
				// 代理执行
				return this.callable.call();
			} finally {
				// 释放Headers
				ThreadFactoryDelegate.this.context.release();
			}
		}
	}
}
