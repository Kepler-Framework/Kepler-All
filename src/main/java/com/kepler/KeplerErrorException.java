package com.kepler;

/**
 * @author KimShen
 *
 */
public class KeplerErrorException extends KeplerRemoteException {

	private static final long serialVersionUID = 1L;

	private final Error e;

	public KeplerErrorException(Error e) {
		super(e);
		this.e = e;
	}

	public Error actual() {
		return this.e;
	}
}
