package com.kepler.protocol.impl;

import com.kepler.serial.hessian.Hessian2Serial;

/**
 * @author KimShen
 *
 */
public class Hessian2RequestFactory extends DefaultRequestFactory {

	@Override
	public byte serial() {
		return Hessian2Serial.SERIAL;
	}

}
