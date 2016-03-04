package com.kepler.invoker.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.util.Assert;

import com.kepler.annotation.Broadcast;
import com.kepler.channel.ChannelContext;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.id.IDGenerator;
import com.kepler.invoker.Invoker;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactory;
import com.kepler.router.Router;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月17日
 */
public class BroadcastInvoker implements Imported, Invoker {

	private static final boolean ACTIVED = PropertiesUtils.get(BroadcastInvoker.class.getName().toLowerCase() + ".actived", false);

	private static final String CANCEL_KEY = BroadcastInvoker.class.getName().toLowerCase() + ".cancel";

	private static final boolean CANCEL_DEF = PropertiesUtils.get(BroadcastInvoker.CANCEL_KEY, true);

	private final MultiKeyMap broadcast = new MultiKeyMap();

	private final ChannelContext context;

	private final RequestFactory request;

	private final IDGenerator generator;

	private final Profile profile;

	private final Router router;

	public BroadcastInvoker(Router router, Profile profile, RequestFactory request, ChannelContext context, IDGenerator generator) {
		this.generator = generator;
		this.profile = profile;
		this.request = request;
		this.context = context;
		this.router = router;
	}

	@Override
	public boolean actived() {
		return BroadcastInvoker.ACTIVED;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		for (Method method : service.service().getMethods()) {
			// 注册Broadcast方法
			Broadcast broadcast = method.getAnnotation(Broadcast.class);
			if (broadcast != null) {
				Assert.state(method.getReturnType().equals(void.class), "Method must return void ... ");
				this.broadcast.put(service, method.getName(), broadcast);
			}
		}
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		// 是否开启了Broadcast, 否则进入下一个Invoker
		return this.broadcast.containsKey(request.service(), request.method()) ? this.broadcast(request) : Invoker.EMPTY;
	}

	/**
	 * 向所有在线主机发送Request
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	private List<Future<Object>> futures(Request request) throws Throwable {
		List<Future<Object>> futures = new ArrayList<Future<Object>>();
		for (Host host : this.router.hosts(request)) {
			// 转换为底层Future(异步)并定向发送Request
			futures.add(Future.class.cast(this.context.get(host).invoke(this.request.request(request, this.generator.generate(request.service(), request.method()), true))));
		}
		return futures;
	}

	/**
	 * 通知所有服务并等待结果, 任意服务出错则抛出异常
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object broadcast(Request request) throws Throwable {
		List<Future<Object>> futures = this.futures(request);
		try {
			for (Future<Object> each : futures) {
				each.get();
			}
			return null;
		} catch (Throwable throwable) {
			// 是否回调Cancel
			if (PropertiesUtils.profile(this.profile.profile(request.service()), BroadcastInvoker.CANCEL_KEY, BroadcastInvoker.CANCEL_DEF)) {
				// 尝试取消剩余任务
				for (Future<Object> each : futures) {
					// Future.cancel本身不抛出异常
					each.cancel(true);
				}
			}
			throw throwable;
		}
	}
}
