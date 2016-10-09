package com.kepler.invoker.forkjoin.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.kepler.KeplerRoutingException;
import com.kepler.KeplerValidateException;
import com.kepler.annotation.ForkJoin;
import com.kepler.annotation.QuietMethod;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.host.Host;
import com.kepler.id.IDGenerators;
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

	/**
	 * 需要Fork Request的Tag集合
	 */
	public static final String TAGS_KEY = ForkJoinInvoker.class.getName().toLowerCase() + ".tags";

	private static final boolean ACTIVED = PropertiesUtils.get(ForkJoinInvoker.class.getName().toLowerCase() + ".actived", false);

	private static final String TAGS_DEF = PropertiesUtils.get(ForkJoinInvoker.TAGS_KEY, Host.TAG_VAL);

	private static final Log LOGGER = LogFactory.getLog(ForkJoinInvoker.class);

	private final MultiKeyMap forkers = new MultiKeyMap();

	private final ThreadPoolExecutor threads;

	private final RequestFactory request;

	private final IDGenerators generators;

	private final MockerContext mocker;

	private final Invoker delegate;

	private final Profile profile;

	private final Forks forks;

	private final Joins joins;

	public ForkJoinInvoker(Forks forks, Joins joins, Invoker delegate, Profile profile, IDGenerators generator, RequestFactory request, MockerContext mocker, ThreadPoolExecutor threads) {
		super();
		this.generators = generator;
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
		try {
			for (Method method : Service.clazz(service).getMethods()) {
				// 注册Fork方法
				ForkJoin forkjoin = method.getAnnotation(ForkJoin.class);
				if (forkjoin != null) {
					Assert.state(!method.getReturnType().equals(void.class), "Method must not return void ... ");
					// 构建ForkJoinInstance
					this.forkers.put(service, method.getName(), new ForkJoinInstance(forkjoin, method.getAnnotation(QuietMethod.class)));
				}
			}
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			ForkJoinInvoker.LOGGER.info("Class not found: " + service);
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
			ForkJoinInstance instance = ForkJoinInstance.class.cast(this.forkers.get(request.service(), request.method()));
			return new ForkJoinProcessor(this.join(instance.forkjoin().join(), request), instance).fork(request, this.fork(instance.forkjoin().fork(), request), this.tag(request)).value();
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

	/**
	 * 异常静默策略
	 * 
	 * @author kim
	 *
	 * 2016年2月22日
	 */
	private interface Quiet {

		public boolean quiet(Throwable throwable);
	}

	private class ForkJoinProcessor {

		/**
		 * Fork任务列表(用于资源释放)
		 */
		private final List<ForkerRunnable> runnables = new ArrayList<ForkerRunnable>();

		/**
		 * 计数器
		 */
		private final AtomicInteger monitor = new AtomicInteger();

		private final Joiner joiner;

		/**
		 * 静默异常
		 */
		private final Quiet quiets;

		/**
		 * 异常
		 */
		volatile private Throwable throwable;

		/**
		 * 最终结果
		 */
		volatile private Object value;

		/**
		 * @param joiner
		 * @param quiets 静默异常
		 */
		private ForkJoinProcessor(Joiner joiner, Quiet quiets) {
			super();
			this.joiner = joiner;
			this.quiets = quiets;
		}

		/**
		 * 释放资源
		 */
		private void release() {
			for (ForkerRunnable runnable : this.runnables) {
				runnable.release();
			}
		}

		/**
		 * 加入任务列表并返回
		 * 
		 * @param actual
		 * @return
		 */
		private ForkJoinProcessor runnable(ForkerRunnable runnable) {
			this.runnables.add(runnable);
			return this;
		}

		/**
		 * 是否存在异常
		 * 
		 * @return
		 * @throws Throwable
		 */
		private ForkJoinProcessor valid() throws Throwable {
			if (this.throwable != null) {
				throw throwable;
			}
			return this;
		}

		public ForkJoinProcessor fork(Request request, Forker forker, String[] tags) {
			for (String tag : tags) {
				// Fork Request Args
				Request actual = ForkJoinInvoker.this.request.request(request, ForkJoinInvoker.this.generators.get(request.service(), request.method()).generate(), forker.fork(request.args(), tag, this.monitor.get()));
				// 指定Header
				actual.put(Host.TAG_KEY, tag);
				ForkJoinInvoker.this.threads.execute(new ForkerRunnable(this, this.monitor, actual));
			}
			return this;
		}

		// 值回调
		public void value(Object value) {
			// This同步
			synchronized (this) {
				this.value = this.joiner.join(this.value, value);
			}
		}

		// 异常回调
		public void throwable(Throwable throwable) {
			// 如果已赋值则不做修改, 如果未赋值则仅非静默异常会被赋值
			this.throwable = (this.throwable == null ? (this.quiets.quiet(throwable) ? null : throwable) : this.throwable);
		}

		public Object value() throws Throwable {
			try {
				// 监听器同步
				synchronized (this.monitor) {
					// 等待监听器,直到计数为0或出现异常或中断
					while (this.monitor.get() > 0 && this.throwable == null) {
						this.monitor.wait();
					}
				}
				return this.valid().value;
			} finally {
				// 释放资源
				this.release();
			}
		}
	}

	private class ForkerRunnable implements Runnable {

		private final ForkJoinProcessor forker;

		private final AtomicInteger monitor;

		private final Request request;

		volatile private Thread thread;

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

		/**
		 * 初始化
		 */
		private ForkerRunnable prepare() {
			// 绑定线程
			this.thread = Thread.currentThread();
			// 如果任务已开始则回调注册
			this.forker.runnable(this);
			return this;
		}

		public ForkerRunnable release() {
			if (this.thread != null) {
				// 尝试中断底层未完成AckFuture
				this.thread.interrupt();
			}
			return this;
		}

		@Override
		public void run() {
			try {
				// 归并结果
				this.prepare().forker.value(ForkJoinInvoker.this.delegate.invoke(this.request));
			} catch (Throwable e) {
				// 回调异常
				synchronized (this.monitor) {
					// 通知ForkJoin主线程出现异常并唤醒
					this.forker.throwable(e);
					this.monitor.notifyAll();
				}
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

	private class ForkJoinInstance implements Quiet {

		private final Collection<Class<? extends Throwable>> quiets;

		private final ForkJoin forkjoin;

		private ForkJoinInstance(ForkJoin forkjoin, QuietMethod quiet) {
			super();
			this.forkjoin = forkjoin;
			// 提取静默异常
			this.quiets = quiet != null ? Arrays.asList(quiet.quiet()) : new HashSet<Class<? extends Throwable>>();
		}

		public ForkJoin forkjoin() {
			return this.forkjoin;
		}

		public boolean quiet(Throwable throwable) {
			return this.quiets.contains(throwable.getClass());
		}
	}
}
