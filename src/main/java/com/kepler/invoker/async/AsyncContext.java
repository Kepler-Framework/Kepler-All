package com.kepler.invoker.async;

import java.util.concurrent.Future;

import com.kepler.KeplerValidateException;
import com.kepler.thread.FutureDelegate;

/**
 * @author kim
 *
 * 2016年2月15日
 */
public class AsyncContext {

	private final static ThreadLocal<AsyncDelegate> DELEGATE = new ThreadLocal<AsyncDelegate>();

	/**
	 * 级联激活
	 */
	private static void valid() {
		if (!AsyncInvoker.ACTIVED) {
			throw new KeplerValidateException("Please setting the AsyncInvoker.actived when using AsyncContext ... ");
		}
	}

	/**
	 * 新建并绑定到当前上下文
	 * 
	 * @param blocking
	 * @return
	 */
	private static AsyncDelegate create(boolean blocking) {
		AsyncDelegate delegate = new AsyncDelegate(new FutureDelegate()).blocking(blocking);
		AsyncContext.DELEGATE.set(delegate);
		return delegate;
	}

	/**
	 * 绑定当前上下文异步转换器
	 * 
	 * @param blocking 原调用是否等待结果
	 * @return
	 */
	public static Future<Object> binding(boolean blocking) {
		AsyncContext.valid();
		// 已绑定则修改Blocking策略后返回, 否则创建
		AsyncDelegate delegate = AsyncContext.DELEGATE.get();
		return delegate != null ? delegate.blocking(blocking).future() : AsyncContext.create(blocking).future();
	}

	/**
	 * 获取并释放
	 * 
	 * @return
	 */
	static AsyncDelegate release() {
		AsyncDelegate delegate = AsyncContext.DELEGATE.get();
		AsyncContext.DELEGATE.set(null);
		AsyncContext.DELEGATE.remove();
		return delegate;
	}
}
