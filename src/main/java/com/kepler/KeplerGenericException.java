package com.kepler;

/**
 * @author kim 2015年8月28日
 */
public class KeplerGenericException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;

	public KeplerGenericException(Throwable e) {
		super(e);
	}
	
	public KeplerGenericException(String reason) {
		super(reason);
	}
}
