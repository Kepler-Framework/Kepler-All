package com.kepler.invoker.impl;

import com.kepler.KeplerErrorException;
import com.kepler.KeplerRemoteException;
import com.kepler.KeplerRoutingException;
import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;
import com.kepler.channel.ChannelContext;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.impl.DefaultDelegate;
import com.kepler.header.Headers;
import com.kepler.host.Host;
import com.kepler.invoker.Invoker;
import com.kepler.invoker.InvokerProcessor;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.protocol.Request;
import com.kepler.router.Router;
import com.kepler.service.ExportedContext;
import com.kepler.trace.TraceCauses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

/**
 * @author kim 2015年7月8日
 */
@Internal
public class ActualInvoker implements Invoker {

	/**
	 * 如果抛出Error异常是否转换为Exception
	 */
	private static final boolean ERROR_TO_EXCEPTION = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".error_to_exception", true);

	/**
	 * None Service重试间隔
	 */
	private static final int INTERVAL = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".interval", 500);

	/**
	 * 是否允许本地调用
	 */
	private static final boolean LOCAL = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".local", true);

	/**
	 * None Service重试阀值
	 */
	private static final int TIMEOUT = PropertiesUtils.get(ActualInvoker.class.getName().toLowerCase() + ".timeout", 3000);

	private static final Log LOGGER = LogFactory.getLog(ActualInvoker.class);

	private final InvokerProcessor processor;

	private final ExportedContext exported;

	private final ChannelContext channels;

	private final MockerContext mocker;

	private final TraceCauses trace;

	private final Router router;

	private int interval = ActualInvoker.INTERVAL;

	private int timeout = ActualInvoker.TIMEOUT;

	public ActualInvoker(InvokerProcessor processor, ExportedContext exported, ChannelContext channels, TraceCauses trace, MockerContext mocker, Router router) {
		super();
		this.processor = processor;
		this.channels = channels;
		this.exported = exported;
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
	public Object invoke(Request request, Method method) throws Throwable {
		try {
			return this.invoker(request, method, System.currentTimeMillis());
		} catch (KeplerRemoteException exception) {
			Throwable cause = exception.cause();
			throw ActualInvoker.ERROR_TO_EXCEPTION && Error.class.isAssignableFrom(cause.getClass()) ? new KeplerErrorException(Error.class.cast(cause)) : exception.cause();
		} finally {
			Headers headers = request.headers();
			if (headers != null) {
				headers.delete(DefaultDelegate.DELEGATE_KEY);
			}
		}
	}

	/**
	 * @param request
	 * @param timestamp 本次执行起始时间
	 * @return
	 * @throws Throwable
	 */
	private Object invoker(Request request, Method method, long timestamp) throws Throwable {
		try {
			Host _host = this.router.host(request);
			Request _request = this.processor.before(request, _host);
			// 如果允许本地调用并且本地存在该服务
			if (ActualInvoker.LOCAL) {
				Invoker invoker = this.exported.get(_request.service());
				if (invoker != null) {
					return invoker.invoke(_request, method);
				}
			}
			return this.channels.get(_host).invoke(_request, method);
		} catch (KeplerRoutingException exception) {
			// 存在Mocker则使用Mocker, 否则重试
            Mocker mocker = this.mocker.get(request.service());
            if (mocker != null) {
                Object mock = mocker.mock(request, method);
                return mock != Invoker.EMPTY ? mock : this.retry(request, method, timestamp, exception);
            } else {
                return this.retry(request, method, timestamp, exception);
            }
		} catch (Throwable throwable) {
			this.trace.put(request, throwable);
			throw throwable;
		}
	}

	private Object retry(Request request, Method method, long timestamp, KeplerRoutingException exception) throws Throwable {
		// 是否终止重试
		this.timeout(timestamp, request, exception);
		ActualInvoker.LOGGER.warn("Warning: " + exception.getMessage() + " then retry ... ");
		Thread.sleep(this.interval);
		// 重试
		return this.invoker(request, method, timestamp);
	}

	private void timeout(long timestamp, Request request, KeplerRoutingException exception) {
		if ((System.currentTimeMillis() - timestamp) > this.timeout) {
			// 服务丢失异常
			this.trace.put(request, exception);
			throw exception;
		}
	}
}
