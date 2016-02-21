package com.kepler.invoker.forkjoin.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.kepler.KeplerRoutingException;
import com.kepler.KeplerValidateException;
import com.kepler.annotation.ForkJoin;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.host.Host;
import com.kepler.id.IDGenerator;
import com.kepler.invoker.Invoker;
import com.kepler.invoker.forkjoin.Forker;
import com.kepler.invoker.forkjoin.Joiner;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * ForkJoin仅消耗1个Threshold限制, 强依赖Headers.ENABLED
 * 
 * @author kim 2016年1月15日
 */
public class ForkJoinInvoker implements Imported, Invoker {

	private final static boolean ACTIVED = PropertiesUtils.get(ForkJoinInvoker.class.getName().toLowerCase() + ".actived", false);

	/**
	 * 需要Fork Request的Tag集合
	 */
	private final static String TAGS_KEY = ForkJoinInvoker.class.getName().toLowerCase() + ".tags";

	private final static String TAGS_DEF = PropertiesUtils.get(ForkJoinInvoker.TAGS_KEY, Host.TAG_VAL);

	private final static Log LOGGER = LogFactory.getLog(ForkJoinInvoker.class);

	private final MultiKeyMap forkers = new MultiKeyMap();

	private final ThreadPoolExecutor threads;

	private final RequestFactory request;

	private final IDGenerator generator;

	private final MockerContext mocker;

	private final Invoker delegate;

	private final Profile profile;

	private final Forks forks;

	private final Joins joins;

	public ForkJoinInvoker(Forks forks, Joins joins, Invoker delegate, Profile profile, IDGenerator generator, RequestFactory request, MockerContext mocker, ThreadPoolExecutor threads) {
		super();
		this.generator = generator;
		this.delegate = delegate;
		this.threads = threads;
		this.profile = profile;
		this.request = request;
		this.mocker = mocker;
		this.forks = forks;
		this.joins = joins;
	}

	@Override
	public boolean actived() {
		return ForkJoinInvoker.ACTIVED && Headers.ENABLED;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		for (Method method : service.service().getMethods()) {
			// 注册Fork方法
			ForkJoin forkjoin = method.getAnnotation(ForkJoin.class);
			if (forkjoin != null) {
				Assert.state(!method.getReturnType().equals(void.class), "Method must not return void ... ");
				this.forkers.put(service, method.getName(), forkjoin);
			}
		}
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		// 是否开启了Fork, 否则进入下一个Invoker
		return this.forkers.containsKey(request.service(), request.method()) ? this.fork(request) : Invoker.EMPTY;
	}

	/**
	 * 获取Forker策略
	 * 
	 * @param name
	 * @param request
	 * @return
	 */
	private Forker fork(String name, Request request) {
		Forker forker = this.forks.get(name);
		if (forker == null) {
			throw new KeplerValidateException("None Forker for " + request);
		}
		return forker;
	}

	/**
	 * 获取Joiner策略
	 * 
	 * @param name
	 * @param request
	 * @return
	 */
	private Joiner join(String name, Request request) {
		Joiner joiner = this.joins.get(name);
		if (joiner == null) {
			throw new KeplerValidateException("None Joiner for " + request);
		}
		return joiner;
	}

	/**
	 * 获取请求标签组
	 * 
	 * @param request
	 * @return
	 */
	private String[] tag(Request request) {
		String tags = PropertiesUtils.profile(this.profile.profile(request.service()), ForkJoinInvoker.TAGS_KEY, ForkJoinInvoker.TAGS_DEF);
		ForkJoinInvoker.LOGGER.info("Fork service " + request.service() + " / " + request.method() + " for tags: " + tags);
		return tags.split(";");
	}

	/**
	 * 分发请求
	 * 
	 * @param request
	 * @return
	 */
	private Object fork(Request request) throws Throwable {
		try {
			ForkJoin forkjoin = ForkJoin.class.cast(this.forkers.get(request.service(), request.method()));
			return new ForkJoinProcessor(this.join(forkjoin.join(), request)).fork(request, this.fork(forkjoin.fork(), request), this.tag(request)).value();
		} catch (KeplerRoutingException exception) {
			return this.mock(request, exception);
		}
	}

	/**
	 * Mock and not retry
	 * 
	 * @param request
	 * @param exception
	 * @return
	 */
	private Object mock(Request request, KeplerRoutingException exception) {
		Mocker mocker = this.mocker.get(request.service());
		if (mocker != null) {
			return mocker.mock(request);
		} else {
			throw exception;
		}
	}

	private class ForkJoinProcessor {

		/**
		 * 计数器
		 */
		private final AtomicInteger monitor = new AtomicInteger();

		private final Joiner joiner;

		/**
		 * 最终结果
		 */
		private Object value;

		private ForkJoinProcessor(Joiner joiner) {
			super();
			this.joiner = joiner;
		}

		public ForkJoinProcessor fork(Request request, Forker forker, String[] tags) {
			for (String tag : tags) {
				try {
					// Fork Request Args
					Request actual = ForkJoinInvoker.this.request.request(request, ForkJoinInvoker.this.generator.generate(request.service(), request.method()), forker.fork(request.args(), tag, this.monitor.get()));
					// 指定Header
					actual.put(Host.TAG_KEY, tag);
					ForkJoinInvoker.this.threads.execute(new ForkerRunnable(this, this.monitor, actual));
				} catch (Throwable e) {
					// 失败仅记录Log
					ForkJoinInvoker.LOGGER.error(e.getMessage(), e);
				}
			}
			return this;
		}

		// 值回调
		public void join(Object value) {
			// This同步
			synchronized (this) {
				this.value = this.joiner.join(this.value, value);
			}
		}

		public Object value() throws Throwable {
			// 监视器同步
			synchronized (this.monitor) {
				// 等待监听器,直到计数为0或中断
				while (this.monitor.get() > 0) {
					this.monitor.wait();
				}
			}
			return this.value;
		}
	}

	private class ForkerRunnable implements Runnable {

		private final ForkJoinProcessor forker;

		private final AtomicInteger monitor;

		private final Request request;

		/**
		 * @param forker
		 * @param monitor 计数器
		 * @param request
		 */
		private ForkerRunnable(ForkJoinProcessor forker, AtomicInteger monitor, Request request) {
			super();
			// 递增监视器
			(this.monitor = monitor).incrementAndGet();
			this.request = request;
			this.forker = forker;
		}

		@Override
		public void run() {
			try {
				// 归并结果
				this.forker.join(ForkJoinInvoker.this.delegate.invoke(this.request));
			} catch (Throwable e) {
				// 失败仅记录Log
				ForkJoinInvoker.LOGGER.error(e.getMessage(), e);
			} finally {
				// 递减监视器,如果为0则唤醒线程
				synchronized (this.monitor) {
					if (this.monitor.decrementAndGet() == 0) {
						this.monitor.notifyAll();
					}
				}
			}
		}
	}
}
