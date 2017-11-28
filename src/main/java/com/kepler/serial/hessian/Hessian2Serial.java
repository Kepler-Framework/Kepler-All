package com.kepler.serial.hessian;

import java.io.InputStream;
import java.io.OutputStream;

import com.kepler.config.PropertiesUtils;
import com.kepler.protocol.RequestFactories;
import com.kepler.protocol.ResponseFactories;

/**
 * 更高压缩比
 * 
 * @author kim 2016年2月2日
 */
public class Hessian2Serial extends HessianSerial {

	/**
	 * 缓冲大小
	 */
	private static final int BUFFER = PropertiesUtils.get(Hessian2Serial.class.getName().toLowerCase() + ".buffer", 0x4 << 6);

	private static final String NAME = "hessian2";

	public static final byte SERIAL = 1;

	public Hessian2Serial(ResponseFactories response, RequestFactories request) {
		super(response, request);
	}

	public String name() {
		return Hessian2Serial.NAME;
	}

	public byte serial() {
		return Hessian2Serial.SERIAL;
	}

	@Override
	protected Integer buffer() {
		return Hessian2Serial.BUFFER;
	}

	@Override
	protected HessianInputProxy input(InputStream stream, Integer buffer) {
		return new HessianInputProxy2(stream, buffer);
	}

	@Override
	protected HessianOutputProxy output(OutputStream stream, Integer buffer) {
		return new HessianOutputProxy2(stream, buffer);
	}
}
