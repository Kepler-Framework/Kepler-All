package com.kepler.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.annotation.QuietMethod;
import com.kepler.annotation.QuietThrowable;
import com.kepler.config.PropertiesUtils;
import com.kepler.method.Methods;
import com.kepler.protocol.Request;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Quiet;
import com.kepler.service.Service;
import com.kepler.trace.Trace;

/**
 * @author kim 2015年12月15日
 */
public class QuietExceptions implements Quiet, Imported, Exported {

	/**
	 * 是否对静默异常本地提示
	 */
	private static final boolean WARNING = PropertiesUtils.get(QuietExceptions.class.getName().toLowerCase() + ".warning", false);

	private static final List<Class<? extends Throwable>> EMPTY = Collections.unmodifiableList(new ArrayList<Class<? extends Throwable>>());

	private static final Log LOGGER = LogFactory.getLog(QuietExceptions.class);

	volatile private Map<Service, QuietMethods> quiets = new HashMap<Service, QuietMethods>();

	private final Methods methods;

	public QuietExceptions(Methods methods) {
		super();
		this.methods = methods;
	}

	/**
	 * 提取Service中所有Method对应静默
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void install(Service service) throws Exception {
		try {
			Map<Service, QuietMethods> quiets = new HashMap<Service, QuietMethods>(this.quiets);
			// 获取对应Method
			QuietMethods methods = this.methods(quiets, service);
			for (Method each : Service.clazz(service).getMethods()) {
				// 标记@QuietMethod则注册静默异常集合
				QuietMethod method = AnnotationUtils.findAnnotation(each, QuietMethod.class);
				methods.put(each, method != null ? Arrays.asList(method.quiet()) : QuietExceptions.EMPTY);
			}
			this.quiets = quiets;
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			QuietExceptions.LOGGER.info("Class not found: " + service);
		}
	}

	/**
	 * 获取服务对应QuietMethods
	 * 
	 * @param service
	 * @return
	 */
	private QuietMethods methods(Map<Service, QuietMethods> quiets, Service service) {
		QuietMethods methods = quiets.get(service);
		// Create if null
		methods = methods != null ? methods : new QuietMethods();
		// 回写
		quiets.put(service, methods);
		return methods;
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		this.install(service);
	}

	@Override
	public void subscribe(Service service) throws Exception {
		this.install(service);
	}

	public void unsubscribe(Service service) throws Exception {
		Map<Service, QuietMethods> quiets = new HashMap<Service, QuietMethods>(this.quiets);
		quiets.remove(service);
		this.quiets = quiets;
	}

	@Override
	public boolean quiet(Request request, Class<? extends Throwable> throwable) {
		try {
			// Guard case, 静默标记异常
			if (AnnotationUtils.findAnnotation(throwable, QuietThrowable.class) != null) {
				return true;
			}
			Method actual = this.methods.method(Service.clazz(request.service()), request.method(), request.types());
			QuietMethods methods = this.quiets.get(request.service());
			// 如果可以获取QuietMethods(非泛化)并且可以获取实际方法则尝试从QuietMethods解析, 如果QuietMethods或实际方法任一无法获得则尝试解析异常
			return (methods != null && actual != null) ? methods.exceptions(actual).contains(throwable) : false;
		} catch (Exception e) {
			QuietExceptions.LOGGER.info(e.getMessage(), e);
			return false;
		}
	}

	public boolean print(Request request, Throwable throwable) {
		Throwable actual = InvocationTargetException.class.isAssignableFrom(throwable.getClass()) ? InvocationTargetException.class.cast(throwable).getTargetException() : throwable;
		String message = "[trace=" + request.get(Trace.TRACE) + "][message=" + actual.getMessage() + "]";
		boolean quiet = this.quiet(request, actual.getClass());
		if (!quiet) {
			// 非静默异常输出ERROR
			QuietExceptions.LOGGER.error(message, actual);
		} else {
			// 静默异常根据配置输出WARN(默认关闭)
			if (QuietExceptions.WARNING) {
				QuietExceptions.LOGGER.warn(message, actual);
			}
		}
		return quiet;
	}

	private class QuietMethods {

		private final Map<Method, List<Class<? extends Throwable>>> quiet = new HashMap<Method, List<Class<? extends Throwable>>>();

		/**
		 * Method -> 对应静默异常
		 * 
		 * @param method
		 * @param throwables
		 * @return
		 */
		private QuietMethods put(Method method, List<Class<? extends Throwable>> throwables) {
			this.quiet.put(method, throwables);
			return this;
		}

		public List<Class<? extends Throwable>> exceptions(Method method) {
			List<Class<? extends Throwable>> exceptions = this.quiet.get(method);
			return exceptions != null ? exceptions : QuietExceptions.EMPTY;
		}
	}
}
