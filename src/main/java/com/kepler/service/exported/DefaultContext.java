package com.kepler.service.exported;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerRemoteException;
import com.kepler.KeplerValidateException;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.protocol.Request;
import com.kepler.service.Exported;
import com.kepler.service.ExportedContext;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class DefaultContext implements ExportedContext, ExportedServices, Exported {

	/**
	 * 是否允许多次发布相同Service
	 */
	private static final boolean CONFLICT = PropertiesUtils.get(DefaultContext.class.getName().toLowerCase() + ".conflict", true);

	private static final Log LOGGER = LogFactory.getLog(DefaultContext.class);

	private final Map<Service, Invoker> invokers = new HashMap<Service, Invoker>();

	private final Map<Service, Object> services = new HashMap<Service, Object>();

	public Invoker get(Service service) {
		return this.invokers.get(service);
	}

	public Map<Service, Object> services() {
		return this.services;
	}

	@Override
	public void exported(Service service, Object instance) {
		this.valid(service);
		this.services.put(service, instance);
		this.invokers.put(service, new ProxyInvoker(instance));
	}

	private void valid(Service service) {
		if (DefaultContext.CONFLICT && this.invokers.containsKey(service)) {
			throw new KeplerValidateException("Duplicate service for: " + service);
		}
	}

	private class ProxyInvoker implements Invoker {

		private final Object service;

		private ProxyInvoker(Object service) {
			super();
			this.service = service;
		}

		@Override
		public Object invoke(Request request) throws Throwable {
			// MethodUtils.getMatchingAccessibleMethod(request.service(), request.method(), request.types()) 获取指定Method
			Method method = MethodUtils.getMatchingAccessibleMethod(request.service().service(), request.method(), request.types());
			try {
				return this.response(request, this.exists(request, method).invoke(this.service, request.args()));
			} catch (NoSuchMethodException exception) {
				DefaultContext.LOGGER.error(exception.getMessage(), exception);
				throw exception;
			} catch (Throwable throwable) {
				DefaultContext.LOGGER.error(throwable.getMessage(), throwable);
				throw this.throwable(method, this.cause(throwable));
			}
		}

		/**
		 * 如果为异步调用则转换为Future并调用Get等待, 否则直接返回
		 * 
		 * @param request
		 * @param response
		 * @return
		 * @throws Exception
		 */
		private Object response(Request request, Object response) throws Exception {
			// 不为Null并且为Future则Block等待结果
			return response != null && Future.class.isAssignableFrom(response.getClass()) ? Future.class.cast(response).get() : response;
		}

		/**
		 * Method是否已定位
		 * 
		 * @param request
		 * @param method
		 * @return
		 * @throws NoSuchMethodException
		 */
		private Method exists(Request request, Method method) throws NoSuchMethodException {
			if (method == null) {
				throw new NoSuchMethodException("No such method: " + request.method());
			}
			return method;
		}

		/**
		 * 如果异常为已声明异常(或其子类)则抛出否则包装为KeplerRemoteException
		 * 
		 * @param method
		 * @param throwable
		 * @return
		 */
		private Throwable throwable(Method method, Throwable throwable) {
			for (Class<?> exception : method.getExceptionTypes()) {
				if (exception.isAssignableFrom(throwable.getClass())) {
					return throwable;
				}
			}
			return new KeplerRemoteException(new StringBuffer().append(throwable.getClass()).append(" (").append(throwable.getMessage()).append(") ").toString());
		}

		/**
		 * 获取Root Exception
		 * 
		 * @param throwable
		 * @return
		 */
		private Throwable cause(Throwable throwable) {
			return throwable.getCause() == null ? throwable : this.cause(throwable.getCause());
		}

		public boolean actived() {
			return true;
		}
	}
}
