package com.kepler.protocol.impl;

import com.kepler.serial.hessian.Hessian1Serial;

/**
 * @author KimShen
 *
 */
public class Hessian1ResponseFactory extends DefaultResponseFactory {

	@Override
	public byte serial() {
		return Hessian1Serial.SERIAL;
	}

}
