package com.kepler.protocol.impl;

import com.kepler.serial.hessian.Hessian1Serial;

/**
 * @author KimShen
 *
 */
public class Hessian1RequestFactory extends DefaultRequestFactory {

	@Override
	public byte serial() {
		return Hessian1Serial.SERIAL;
	}

}
