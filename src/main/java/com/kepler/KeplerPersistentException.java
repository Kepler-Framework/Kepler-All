package com.kepler;

/**
 * @author KimShen
 *
 */
public class KeplerPersistentException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;

	public KeplerPersistentException(Throwable e) {
		super(e);
	}

	public KeplerPersistentException(String e) {
		super(e);
	}
}
