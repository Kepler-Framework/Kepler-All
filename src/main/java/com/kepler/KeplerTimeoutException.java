package com.kepler;

/**
 * @author kim 2015年11月6日
 */
public class KeplerTimeoutException extends KeplerRemoteException {

	private static final long serialVersionUID = 1L;

	public KeplerTimeoutException(String e) {
		super(e);
	}
}
