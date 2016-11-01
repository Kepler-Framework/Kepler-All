package com.kepler;

import com.kepler.config.PropertiesUtils;

/**
 * @author kim
 *
 * 2016年2月17日
 */
public class KeplerException extends RuntimeException {

	private static final boolean STACK = PropertiesUtils.get(KeplerException.class.getName().toLowerCase() + ".stack_trace", false);

	private static final long serialVersionUID = 1L;

	public KeplerException(String e) {
		super(e);
	}

	public KeplerException(Throwable e) {
		super(e);
	}

	public Throwable fillInStackTrace() {
		// 关闭Stack Trace则直接返回
		return KeplerException.STACK ? super.fillInStackTrace() : null;
	}
}
