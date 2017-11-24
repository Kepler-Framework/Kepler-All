package com.kepler.invoker.impl;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerRemoteException;
import com.kepler.KeplerValidateException;
import com.kepler.annotation.Compete;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.method.Methods;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.router.Router;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * 同时发起多次请求并仅采用最快返回结果, 消耗1个Threshold限制
 * 
 * @author kim 2016年1月19日
 */
public class CompeteInvoker implements Imported, Invoker {

	public static final String CANCEL_KEY = CompeteInvoker.class.getName().toLowerCase() + ".cancel";

	private static final boolean ACTIVED = PropertiesUtils.get(CompeteInvoker.class.getName().toLowerCase() + ".actived", false);

	private static final boolean CANCEL_DEF = PropertiesUtils.get(CompeteInvoker.CANCEL_KEY, true);

	private static final String SPAN = CompeteInvoker.class.getName().toLowerCase() + ".span";

	private static final Log LOGGER = LogFactory.getLog(CompeteInvoker.class);

	volatile private MultiKeyMap competed = new MultiKeyMap();

	private final ThreadPoolExecutor threads;

	private final RequestFactory request;

	private final IDGenerators generators;

	private final Invoker delegate;

	private final Methods methods;

	private final Profile profile;

	private final Router router;

	public CompeteInvoker(ThreadPoolExecutor threads, RequestFactory request, IDGenerators generators, Methods methods, Invoker delegate, Profile profile, Router router) {
		super();
		this.generators = generators;
		this.delegate = delegate;
		this.methods = methods;
		this.request = request;
		this.profile = profile;
		this.threads = threads;
		this.router = router;
	}

	@Override
	public boolean actived() {
		return CompeteInvoker.ACTIVED;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		try {
			MultiKeyMap competed = new MultiKeyMap();
			competed.putAll(this.competed);
			for (Method method : Service.clazz(service).getMethods()) {
				// 注册Compete方法
				Compete compete = method.getAnnotation(Compete.class);
				if (compete != null) {
					competed.put(service, method.getName(), compete);
					CompeteInvoker.LOGGER.info("[subscribe][service=" + service + "][method=" + method + "]");
				}
			}
			this.competed = competed;
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			CompeteInvoker.LOGGER.info("Class not found: " + service);
		}
	}

	public void unsubscribe(Service service) throws Exception {
		try {
			MultiKeyMap competed = new MultiKeyMap();
			competed.putAll(this.competed);
			for (Method method : Service.clazz(service).getMethods()) {
				competed.removeMultiKey(service, method);
				CompeteInvoker.LOGGER.info("[unsubscribe][service=" + service + "][method=" + method + "]");
			}
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			CompeteInvoker.LOGGER.info("Class not found: " + service);
		}
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		// 是否开启了Compete, 否则进入下一个Invoker
		return this.competed.containsKey(request.service(), this.methods.method(Service.clazz(request.service()), request.method(), request.types())) ? this.compete(request) : Invoker.EMPTY;
	}

	private Object compete(Request request) throws Throwable {
		CompeteService service = new CompeteService(this.threads, request);
		for (int index = 0; index < service.capacity(); index++) {
			// Clone Request
			service.submit(new CompeteCallable(this.request.request(request, this.generators.get(request.service(), request.method()).generate()), index));
		}
		// 如果没有任何提交则抛出异常,避免死锁
		return service.valid().select();
	}

	private class CompeteService extends ExecutorCompletionService<Object> {

		private final Future<Object>[] futures;

		private final Request request;

		private int submited = 0;

		@SuppressWarnings("unchecked")
		private CompeteService(Executor executor, Request request) {
			super(executor);
			this.request = request;
			// 计算Future数量. 指定Span与最大主机数量的最小值
			this.futures = new Future[Math.min(CompeteInvoker.this.router.hosts(this.request).size(), PropertiesUtils.profile(CompeteInvoker.this.profile.profile(this.request.service()), CompeteInvoker.SPAN, Compete.class.cast(CompeteInvoker.this.competed.get(this.request.service(), this.request.method())).span()))];
		}

		public int capacity() {
			return this.futures.length;
		}

		public void submit(CompeteCallable task) {
			this.futures[task.index()] = super.submit(task);
			this.submited++;
		}

		public Object select() throws Exception {
			try {
				return super.take().get();
			} finally {
				// 是否回调Cancel
				if (PropertiesUtils.profile(CompeteInvoker.this.profile.profile(this.request.service()), CompeteInvoker.CANCEL_KEY, CompeteInvoker.CANCEL_DEF)) {
					// 尝试取消剩余任务
					for (Future<Object> each : this.futures) {
						// Future.cancel本身不抛出异常
						each.cancel(true);
					}
				}
			}
		}

		public CompeteService valid() throws KeplerValidateException {
			if (this.submited == 0) {
				throw new KeplerValidateException("None submit for " + this.request);
			}
			return this;
		}
	}

	private class CompeteCallable implements Callable<Object> {

		private final Request request;

		private final int index;

		private CompeteCallable(Request request, int index) {
			super();
			this.request = request;
			this.index = index;
		}

		public int index() {
			return this.index;
		}

		@Override
		public Object call() throws Exception {
			try {
				return CompeteInvoker.this.delegate.invoke(this.request);
			} catch (Throwable e) {
				throw Exception.class.isAssignableFrom(e.getClass()) ? Exception.class.cast(e) : new KeplerRemoteException(e);
			}
		}
	}
}
