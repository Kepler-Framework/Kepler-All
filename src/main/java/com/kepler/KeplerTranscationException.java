package com.kepler;

/**
 * @author KimShen
 *
 */
public class KeplerTranscationException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;

	public KeplerTranscationException(Throwable e) {
		super(e);
	}

	public KeplerTranscationException(String e) {
		super(e);
	}
}
