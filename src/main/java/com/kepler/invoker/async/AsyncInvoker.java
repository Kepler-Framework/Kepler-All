package com.kepler.invoker.async;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.kepler.annotation.Async;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.method.Methods;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @Async标签处理
 * 
 * @author kim 2015年10月31日
 */
public class AsyncInvoker implements Imported, Invoker {

	static final boolean ACTIVED = PropertiesUtils.get(AsyncInvoker.class.getName().toLowerCase() + ".actived", false);

	private static final Log LOGGER = LogFactory.getLog(AsyncInvoker.class);

	/**
	 * 已注册Service, Method(缓存)
	 */
	private final Map<Service, Set<Method>> async = new HashMap<Service, Set<Method>>();

	private final RequestFactory factory;

	private final Invoker delegate;

	private final Methods methods;

	public AsyncInvoker(RequestFactory factory, Methods methods, Invoker delegate) {
		super();
		this.factory = factory;
		this.methods = methods;
		this.delegate = delegate;
	}

	@Override
	public boolean actived() {
		return AsyncInvoker.ACTIVED;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		try {
			Set<Method> methods = new HashSet<Method>();
			for (Method method : Service.clazz(service).getMethods()) {
				// 注册异步方法(@Async && return void)
				if (method.getAnnotation(Async.class) != null) {
					Assert.state(method.getReturnType().equals(void.class), "Method must return void ... ");
					methods.add(method);
				}
			}
			this.async.put(service, methods);
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			AsyncInvoker.LOGGER.info("Class not found: " + service);
		}
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		AsyncDelegate delegate = AsyncContext.release();
		// 当前请求支持异步或使用异步上下文则尝试,否则继续下一个Invoker
		return delegate != null || this.async.get(request.service()).contains(this.methods.method(Service.clazz(request.service()), request.method(), request.types())) ? this.invoke(request, delegate) : Invoker.EMPTY;
	}

	private Object invoke(Request request, AsyncDelegate delegate) throws Throwable {
		// 异步策略
		return delegate == null ? this.async(request) : this.async(request, delegate);
	}

	/**
	 * @Async标签处理
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object async(Request request) throws Throwable {
		// 修改Request为异步并发送
		AsyncInvoker.this.delegate.invoke(AsyncInvoker.this.factory.request(request, request.ack(), true));
		// 异步无返回值
		return null;
	}

	/**
	 * AsyncDelegate处理
	 * 
	 * @param request
	 * @param delegate
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	private Object async(Request request, AsyncDelegate delegate) throws Throwable {
		try {
			// 修改请求为异步, 并发送后获取原始Future(AckFuture)
			delegate.future().binding(Future.class.cast(AsyncInvoker.this.delegate.invoke(AsyncInvoker.this.factory.request(request, request.ack(), true))));
		} catch (Throwable throwable) {
			// 任何异常释放delegate
			delegate.future().release(throwable);
		}
		return delegate.blocking() ? delegate.future().get() : null;
	}
}
