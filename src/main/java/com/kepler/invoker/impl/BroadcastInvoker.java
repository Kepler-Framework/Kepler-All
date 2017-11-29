package com.kepler.invoker.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.kepler.annotation.Broadcast;
import com.kepler.channel.ChannelContext;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.id.IDGenerators;
import com.kepler.invoker.Invoker;
import com.kepler.method.Methods;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactories;
import com.kepler.router.Router;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月17日
 */
public class BroadcastInvoker implements Imported, Invoker {

	public static final String CANCEL_KEY = BroadcastInvoker.class.getName().toLowerCase() + ".cancel";

	private static final boolean ACTIVED = PropertiesUtils.get(BroadcastInvoker.class.getName().toLowerCase() + ".actived", false);

	private static final boolean CANCEL_DEF = PropertiesUtils.get(BroadcastInvoker.CANCEL_KEY, true);

	private static final Log LOGGER = LogFactory.getLog(BroadcastInvoker.class);

	volatile private MultiKeyMap broadcast = new MultiKeyMap();

	private final RequestFactories request;

	private final ChannelContext context;

	private final IDGenerators generators;

	private final Methods methods;

	private final Profile profile;

	private final Router router;

	public BroadcastInvoker(Router router, Profile profile, Methods methods, RequestFactories request, ChannelContext context, IDGenerators generators) {
		this.generators = generators;
		this.methods = methods;
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
		try {
			MultiKeyMap broadcast = new MultiKeyMap();
			broadcast.putAll(this.broadcast);
			for (Method method : Service.clazz(service).getMethods()) {
				Broadcast annotation = method.getAnnotation(Broadcast.class);
				if (annotation != null) {
					Assert.state(method.getReturnType().equals(void.class), "Method must return void ... ");
					BroadcastInvoker.LOGGER.info("[subscribe][service=" + service + "][method=" + method + "]");
					broadcast.put(service, method, annotation);
				}
			}
			this.broadcast = broadcast;
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			BroadcastInvoker.LOGGER.info("Class not found: " + service);
		}
	}

	public void unsubscribe(Service service) throws Exception {
		try {
			MultiKeyMap broadcast = new MultiKeyMap();
			broadcast.putAll(this.broadcast);
			for (Method method : Service.clazz(service).getMethods()) {
				broadcast.removeMultiKey(service, method);
			}
			this.broadcast = broadcast;
			BroadcastInvoker.LOGGER.info("[unsubscribe][service=" + service + "]");
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			BroadcastInvoker.LOGGER.info("Class not found: " + service);
		}
	}

	@Override
	public Object invoke(Request request, Method method) throws Throwable {
		// 是否开启了Broadcast, 否则进入下一个Invoker
		return this.broadcast.containsKey(request.service(), this.methods.method(Service.clazz(request.service()), request.method(), request.types())) ? this.broadcast(request, method) : Invoker.EMPTY;
	}

	/**
	 * 向所有在线主机发送Request
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	private List<Future<Object>> futures(Request request, Method method) throws Throwable {
		List<Future<Object>> futures = new ArrayList<Future<Object>>();
		for (Host host : this.router.hosts(request)) {
			// 转换为底层Future(异步)并定向发送Request
			futures.add(Future.class.cast(this.context.get(host).invoke(this.request.factory(request.serial()).request(request, this.generators.get(request.service(), request.method()).generate(), true), method)));
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
	private Object broadcast(Request request, Method method) throws Throwable {
		List<Future<Object>> futures = this.futures(request, method);
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
