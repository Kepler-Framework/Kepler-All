package com.kepler.thread;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerLocalException;

/**
 * 代理Future
 * Warning: 监视器使用this避免创建无用对象(协商)
 * 
 * @author kim 2016年1月15日
 */
public class FutureDelegate implements Serializable, Future<Object> {

	private static final Log LOGGER = LogFactory.getLog(FutureDelegate.class);

	private static final long serialVersionUID = 1L;

	volatile private Future<Object> actual;

	/**
	 * Throwable for release
	 */
	volatile private Throwable throwable;

	/**
	 * 是否绑定
	 */
	volatile private boolean binding;

	/**
	 * 是否释放
	 */
	volatile private boolean release;

	/**
	 * 异常校验
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private FutureDelegate check4exception() {
		if (this.throwable != null) {
			// 如果为Runtime则直接排除否则包装为KeplerLocalException
			if (RuntimeException.class.isAssignableFrom(this.throwable.getClass())) {
				throw RuntimeException.class.cast(this.throwable);
			}
			throw new KeplerLocalException(this.throwable);
		}
		return this;
	}

	/**
	 * 等待, 直到绑定或释放
	 * 
	 * @throws InterruptedException
	 */
	private FutureDelegate waiting() throws InterruptedException {
		synchronized (this) {
			// 尚未绑定, 尚未释放则等待
			while (!this.binding && !this.release) {
				FutureDelegate.LOGGER.debug("Waiting future binding ... ");
				this.wait();
			}
		}
		return this;
	}

	public Future<Object> release(Throwable throwable) {
		synchronized (this) {
			// 标记释放
			this.release = true;
			// 标记异常
			this.throwable = throwable;
			this.notifyAll();
		}
		return this.actual;
	}

	public Future<Object> binding(Future<Object> future) {
		synchronized (this) {
			this.actual = future;
			// 标记绑定
			this.binding = true;
			this.notifyAll();
		}
		return this.actual;
	}

	@Override
	public boolean cancel(boolean interrupt) {
		return this.actual == null ? false : this.actual.cancel(interrupt);
	}

	@Override
	public boolean isCancelled() {
		return this.actual == null ? false : this.actual.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.actual == null ? false : this.actual.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		try {
			return this.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		// Waiting, 并在调用实际Future前对错误进行校验
		this.waiting().check4exception();
		return this.actual == null ? null : this.actual.get(timeout, unit);
	}
}
