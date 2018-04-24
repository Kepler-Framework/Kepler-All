package com.kepler.serial.hessian;

import java.io.InputStream;
import java.io.OutputStream;

import com.kepler.config.PropertiesUtils;
import com.kepler.protocol.RequestFactories;
import com.kepler.protocol.ResponseFactories;
import com.kepler.serial.SerialID;

/**
 * 更高压缩比
 * 
 * @author kim 2016年2月2日
 */
public class Hessian1Serial extends HessianSerial {

	/**
	 * 缓冲大小
	 */
	private static final int BUFFER = PropertiesUtils.get(Hessian1Serial.class.getName().toLowerCase() + ".buffer", 0x4 << 6);

	public static final byte SERIAL = 0;

	public Hessian1Serial(ResponseFactories response, RequestFactories request) {
		super(response, request);
	}

	public String name() {
		return SerialID.SERIAL_DEF;
	}

	public byte serial() {
		return Hessian1Serial.SERIAL;
	}

	public boolean actived() {
		return true;
	}

	@Override
	protected Integer buffer() {
		return Hessian1Serial.BUFFER;
	}

	@Override
	protected HessianInputProxy input(InputStream stream, Integer buffer) {
		return new HessianInputProxy1(stream, buffer);
	}

	@Override
	protected HessianOutputProxy output(OutputStream stream, Integer buffer) {
		return new HessianOutputProxy1(stream, buffer);
	}
}
