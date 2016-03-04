package com.kepler;

/**
 * @author kim 2015年7月8日
 */
public class KeplerValidateException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;

	public KeplerValidateException(String e) {
		super(e);
	}

	public KeplerValidateException(Throwable e) {
		super(e);
	}
}
