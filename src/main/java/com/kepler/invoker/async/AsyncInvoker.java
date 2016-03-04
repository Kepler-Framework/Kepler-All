package com.kepler.invoker.async;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.springframework.util.Assert;

import com.kepler.annotation.Async;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
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

	/**
	 * 已注册Service, Method
	 */
	private final Map<Service, Set<String>> async = new HashMap<Service, Set<String>>();

	private final RequestFactory factory;

	private final Invoker delegate;

	public AsyncInvoker(RequestFactory factory, Invoker delegate) {
		super();
		this.factory = factory;
		this.delegate = delegate;
	}

	@Override
	public boolean actived() {
		return AsyncInvoker.ACTIVED;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		Set<String> methods = new HashSet<String>();
		for (Method method : service.service().getMethods()) {
			// 注册异步方法(@Async && return void)
			if (method.getAnnotation(Async.class) != null) {
				Assert.state(method.getReturnType().equals(void.class), "Method must return void ... ");
				methods.add(method.getName());
			}
		}
		this.async.put(service, methods);
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		AsyncDelegate delegate = AsyncContext.release();
		// 当前Request支持异步或使用异步上下文则尝试,否则继续下一个Invoker
		return delegate != null || this.async.get(request.service()).contains(request.method()) ? this.invoke(request, delegate) : Invoker.EMPTY;
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
			// 修改Request为异步, 并发送后获取原始Future(AckFuture)
			delegate.future().binding(Future.class.cast(AsyncInvoker.this.delegate.invoke(AsyncInvoker.this.factory.request(request, request.ack(), true))));
		} catch (Throwable throwable) {
			// 任何异常释放delegate
			delegate.future().release(throwable);
		}
		return delegate.blocking() ? delegate.future().get() : null;
	}
}
