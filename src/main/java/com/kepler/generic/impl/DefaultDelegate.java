package com.kepler.generic.impl;

import com.kepler.generic.GenericArg;
import com.kepler.generic.GenericDelegate;
import com.kepler.generic.GenericMarker;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class DefaultDelegate implements GenericMarker, GenericDelegate {

	/**
	 * Header Key, 用于服务端判定
	 */
	private static final String DELEGATE_KEY = DefaultDelegate.class.getName().toLowerCase() + ".delegate";

	private static final String DELEGATE_VAL = "";

	@Override
	public boolean marked(Headers headers) {
		try {
			// 从Header中获取并对比
			return headers != null && StringUtils.equals(headers.get(DefaultDelegate.DELEGATE_KEY), DefaultDelegate.DELEGATE_VAL);
		} finally {
			if (headers != null) {
				// 清空Header防止调用链错误
				headers.put(DefaultDelegate.DELEGATE_KEY, null);
			}
		}
	}

	@Override
	public Headers mark(Headers headers) {
		return headers.put(DefaultDelegate.DELEGATE_KEY, DefaultDelegate.DELEGATE_VAL);
	}

	@Override
	public Object delegate(Object service, String method, Object... args) throws Throwable {
		// 代理执行
		return MethodUtils.invokeMethod(service, method, new Args(args).args());
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