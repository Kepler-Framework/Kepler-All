package com.kepler.thread;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 包装Object为Future
 * 
 * @author kim 2016年1月15日
 */
public class FutureWrapper<T> implements Serializable, Future<T> {

	private static final long serialVersionUID = 1L;

	private final T future;

	public FutureWrapper(T future) {
		super();
		this.future = future;
	}

	@Override
	public boolean cancel(boolean interrupt) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public T get() {
		return this.future;
	}

	@Override
	public T get(long timeout, TimeUnit unit) {
		return this.future;
	}
}
