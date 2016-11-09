package com.kepler.generic.wrap.impl;

import java.lang.reflect.InvocationTargetException;

import com.kepler.KeplerGenericException;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericInvoker;
import com.kepler.generic.GenericMarker;
import com.kepler.generic.GenericResponse;
import com.kepler.generic.GenericResponseFactory;
import com.kepler.generic.impl.DefaultMarker;
import com.kepler.generic.wrap.GenericArg;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestValidation;
import com.kepler.service.Quiet;

/**
 * @author KimShen
 *
 */
public class DefaultDelegate extends DefaultMarker implements GenericMarker, GenericInvoker, GenericDelegate {

	private static final boolean ACTIVED = PropertiesUtils.get(DefaultDelegate.class.getName().toLowerCase() + ".actived", false);

	/**
	 * Header Key, 用于服务端判定
	 */
	private static final String DELEGATE_KEY = DefaultDelegate.class.getName().toLowerCase() + ".delegate";

	private static final String DELEGATE_VAL = "generic_wrap";

	private final GenericResponseFactory factory;

	private final RequestValidation validation;

	private final Quiet quiet;

	public DefaultDelegate(GenericResponseFactory factory, RequestValidation validation, Quiet quiet) {
		this.validation = validation;
		this.factory = factory;
		this.quiet = quiet;
	}

	protected String key() {
		return DefaultDelegate.DELEGATE_KEY;
	}

	protected String value() {
		return DefaultDelegate.DELEGATE_VAL;
	}

	public boolean actived() {
		return DefaultDelegate.ACTIVED;
	}

	@Override
	public DefaultDelegate marker() {
		return this;
	}

	@Override
	public DefaultDelegate delegate() {
		return this;
	}

	@Override
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException {
		// 代理执行
		try {
			return this.factory.response(MethodUtils.invokeMethod(instance, method, this.validation.valid(new Args(request.args()).args())));
		} catch (InvocationTargetException e) {
			throw new KeplerGenericException(e.getTargetException());
		} catch (Throwable e) {
			// 尝试输出本地日志
			this.quiet.print(request, e);
			throw new KeplerGenericException(e);
		}
	}

	/**
	 * GenericArg转换为实际参数
	 * 
	 * @author KimShen
	 *
	 */
	private class Args {

		private final Object[] args;

		private Args(Object... args) throws Exception {
			// 标齐长度
			this.args = new Object[args.length];
			for (int index = 0; index < args.length; index++) {
				Object value = args[index];
				// 如果为代理参数则转换否则直接赋值
				this.args[index] = GenericArg.class.isAssignableFrom(value.getClass()) ? GenericArg.class.cast(value).arg() : value;
			}
		}

		/**
		 * 获取实际参数
		 * 
		 * @return
		 */
		public Object[] args() {
			return this.args;
		}
	}
}