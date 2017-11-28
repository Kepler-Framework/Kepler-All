package com.kepler.protocol.impl;

import com.kepler.serial.hessian.Hessian2Serial;

/**
 * @author KimShen
 *
 */
public class Hessian2ResponseFactory extends DefaultResponseFactory {

	@Override
	public byte serial() {
		return Hessian2Serial.SERIAL;
	}
}
