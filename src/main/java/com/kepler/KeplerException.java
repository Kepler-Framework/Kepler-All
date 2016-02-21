package com.kepler;

/**
 * @author kim
 *
 * 2016年2月17日
 */
public class KeplerException extends RuntimeException {

	private final static long serialVersionUID = 1L;

	public KeplerException(String e) {
		super(e);
	}

	public KeplerException(Throwable e) {
		super(e);
	}
}
