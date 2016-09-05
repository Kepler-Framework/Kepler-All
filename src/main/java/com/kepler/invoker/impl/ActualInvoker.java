package com.kepler.invoker.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerRemoteException;
import com.kepler.KeplerRoutingException;
import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;
import com.kepler.channel.ChannelContext;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.protocol.Request;
import com.kepler.router.Router;
import com.kepler.trace.TraceCollector;

/**
 * @author kim 2015年7月8日
 */
@Internal
public class ActualInvoker implements Invoker {

	/**
	 * None Service重试间隔
	 */
	private static final int INTERVAL = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".interval", 500);

	/**
	 * None Service重试阀值
	 */
	private static final int TIMEOUT = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".timeout", 3000);

	private static final Log LOGGER = LogFactory.getLog(ActualInvoker.class);

	private final ChannelContext channels;

	private final TraceCollector trace;

	private final MockerContext mocker;

	private final Router router;

	private int interval = ActualInvoker.INTERVAL;

	private int timeout = ActualInvoker.TIMEOUT;

	public ActualInvoker(ChannelContext channels, TraceCollector trace, MockerContext mocker, Router router) {
		super();
		this.channels = channels;
		this.router = router;
		this.mocker = mocker;
		this.trace = trace;
	}

	@Config(value = "com.kepler.invoker.impl.actualinvoker.interval")
	public void interval(int interval) {
		this.interval = interval;
	}

	@Config(value = "com.kepler.invoker.impl.actualinvoker.timeout")
	public void timeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public boolean actived() {
		return true;
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		try {
			return this.invoker(request, System.currentTimeMillis());
		} catch (KeplerRemoteException exception) {
			throw exception.cause();
		}
	}

	/**
	 * @param request
	 * @param timestamp 本次执行起始时间
	 * @return
	 * @throws Throwable
	 */
	private Object invoker(Request request, long timestamp) throws Throwable {
		try {
			return this.channels.get(this.router.host(request)).invoke(request);
		} catch (KeplerRoutingException exception) {
			// 存在Mocker则使用Mocker, 否则重试
			Mocker mocker = this.mocker.get(request.service());
			return mocker != null ? mocker.mock(request) : this.retry(request, timestamp, exception);
		} catch (Throwable throwable) {
			this.trace.put(request.service(), request.method());
			throw throwable;
		}
	}

	private Object retry(Request request, long timestamp, KeplerRoutingException exception) throws Throwable {
		// 是否终止重试
		this.timeout(timestamp, exception);
		ActualInvoker.LOGGER.warn("Warning: " + exception.getMessage() + " then retry ... ");
		Thread.sleep(this.interval);
		// 重试
		return this.invoker(request, timestamp);
	}

	private void timeout(long timestamp, KeplerRoutingException exception) {
		if ((System.currentTimeMillis() - timestamp) > this.timeout) {
			throw exception;
		}
	}
}
