package com.kepler.service.imported;

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
import com.kepler.method.Methods;
import com.kepler.protocol.Request;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Quiet;
import com.kepler.service.Service;

/**
 * @author kim 2015年12月15日
 */
public class QuietExceptions implements Quiet, Imported, Exported {

	private static final List<Class<? extends Throwable>> EMPTY = Collections.unmodifiableList(new ArrayList<Class<? extends Throwable>>());

	private static final Log LOGGER = LogFactory.getLog(QuietExceptions.class);

	private final Map<Service, QuietMethods> methods = new HashMap<Service, QuietMethods>();

	private final Methods mehtods;

	public QuietExceptions(Methods mehtods) {
		super();
		this.mehtods = mehtods;
	}

	/**
	 * 获取服务对应QuietMethods
	 * 
	 * @param service
	 * @return
	 */
	private QuietMethods methods(Service service) {
		QuietMethods methods = this.methods.get(service);
		// Create if null
		methods = methods != null ? methods : new QuietMethods();
		// 回写
		this.methods.put(service, methods);
		return methods;
	}

	/**
	 * 提取Service中所有Method对应静默
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void quiet(Service service) throws Exception {
		try {
			// 获取对应Method
			QuietMethods methods = this.methods(service);
			for (Method each : Service.clazz(service).getMethods()) {
				// Method : Quite List
				methods.put(each, this.quiet(AnnotationUtils.findAnnotation(each, QuietMethod.class)));
			}
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			QuietExceptions.LOGGER.info("Class not found: " + service);
		}
	}

	/**
	 * 标记@QuietMethod则注册静默异常集合
	 * 
	 * @param each
	 * @param quiet
	 */
	private List<Class<? extends Throwable>> quiet(QuietMethod quiet) {
		return quiet != null ? Arrays.asList(quiet.quiet()) : QuietExceptions.EMPTY;
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		this.quiet(service);
	}

	@Override
	public void subscribe(Service service) throws Exception {
		this.quiet(service);
	}

	@Override
	public boolean quiet(Request request, Class<? extends Throwable> throwable) {
		try {
			// Guard case, 静默标记异常
			if (AnnotationUtils.findAnnotation(throwable, QuietThrowable.class) != null) {
				return true;
			}
			Method actual = this.mehtods.method(Service.clazz(request.service()), request.method(), request.types());
			QuietMethods methods = this.methods.get(request.service());
			// 如果可以获取QuietMethods(非泛化)并且可以获取实际方法则尝试从QuietMethods解析, 如果QuietMethods或实际方法任一无法获得则尝试解析异常
			return (methods != null && actual != null) ? methods.exceptions(actual).contains(throwable) : false;
		} catch (Exception e) {
			QuietExceptions.LOGGER.info(e.getMessage(), e);
			return false;
		}
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
