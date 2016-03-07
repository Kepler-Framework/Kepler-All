package com.kepler;

/**
 * @author kim 2015年7月8日
 */
public class KeplerLocalException extends KeplerException {

	private static final long serialVersionUID = 1L;

	public KeplerLocalException(String e) {
		super(e);
	}

	public KeplerLocalException(Throwable e) {
		super(e);
	}
}
