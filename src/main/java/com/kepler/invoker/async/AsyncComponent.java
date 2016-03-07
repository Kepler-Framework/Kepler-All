package com.kepler.invoker.async;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim
 *
 * 2016年2月16日
 */
abstract public class AsyncComponent {

	/**
	 * 是否在Future()线程中显示错误日志(Warn)
	 */
	private static final boolean WARNING = PropertiesUtils.get(AsyncComponent.class.getName().toLowerCase() + ".warning", false);

	private static final Log LOGGER = LogFactory.getLog(AsyncComponent.class);

	// 创建线程Future, 标记原方法非堵塞
	private final Future<Object> future = AsyncContext.binding(false);

	/**
	 * 代理Cancel
	 * 
	 * @param interrupt
	 * @return
	 */
	public boolean cancel(boolean interrupt) {
		return this.future.cancel(interrupt);
	}

	public Future<Object> future() {
		try {
			// 没有完成(或超时/异常)且没有被取消则回调取值
			if (!this.future.isDone() && !this.future.isCancelled()) {
				this.call();
			}
		} catch (Throwable throwable) {
			if (AsyncComponent.WARNING) {
				AsyncComponent.LOGGER.warn(throwable.getMessage(), throwable);
			}
		}
		return this.future;
	}

	abstract protected void call() throws Throwable;
}
