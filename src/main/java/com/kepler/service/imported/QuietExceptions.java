package com.kepler.service.imported;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.annotation.QuietMethod;
import com.kepler.protocol.Request;
import com.kepler.service.Imported;
import com.kepler.service.Quiet;
import com.kepler.service.Service;

/**
 * @author kim 2015年12月15日
 */
public class QuietExceptions implements Quiet, Imported {

	private static final List<Class<? extends Throwable>> EMPTY = Collections.unmodifiableList(new ArrayList<Class<? extends Throwable>>());

	private final Map<Service, QuietMethods> methods = new HashMap<Service, QuietMethods>();

	/**
	 * 标记@QuietMethod则注册静默异常
	 * 
	 * @param each
	 * @param quiet
	 */
	private List<Class<? extends Throwable>> quiet(QuietMethod quiet) {
		return quiet != null ? Arrays.asList(quiet.quiet()) : QuietExceptions.EMPTY;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		QuietMethods methods = new QuietMethods();
		for (Method each : Service.clazz(service).getMethods()) {
			// Method Name : Quite List
			methods.put(each.getName(), this.quiet(AnnotationUtils.findAnnotation(each, QuietMethod.class)));
		}
		this.methods.put(service, methods);
	}

	@Override
	public boolean quiet(Request request, Class<? extends Throwable> throwable) {
		// 当前Throwable是否为指定Service指定Method的静默异常
		return this.methods.get(request.service()).exceptions(request.method()).contains(throwable);
	}

	private class QuietMethods {

		private final Map<String, List<Class<? extends Throwable>>> quiet = new HashMap<String, List<Class<? extends Throwable>>>();

		/**
		 * Method -> 对应静默异常
		 * 
		 * @param method
		 * @param throwables
		 * @return
		 */
		private QuietMethods put(String method, List<Class<? extends Throwable>> throwables) {
			this.quiet.put(method, throwables);
			return this;
		}

		public List<Class<? extends Throwable>> exceptions(String method) {
			List<Class<? extends Throwable>> exceptions = this.quiet.get(method);
			return exceptions != null ? exceptions : QuietExceptions.EMPTY;
		}
	}
}
