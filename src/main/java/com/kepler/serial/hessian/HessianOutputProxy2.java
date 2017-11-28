package com.kepler.serial.hessian;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.kepler.com.caucho.hessian.io.Hessian2Output;
import com.kepler.com.caucho.hessian.io.SerializerFactory;

/**
 * @author KimShen
 *
 */
public class HessianOutputProxy2 implements HessianOutputProxy {

	private final Hessian2Output output;

	public HessianOutputProxy2(OutputStream stream, int buffer) {
		this.output = new Hessian2Output(new BufferedOutputStream(stream, buffer));
	}

	public HessianOutputProxy setSerializerFactory(SerializerFactory factory) {
		this.output.setSerializerFactory(factory);
		return this;
	}

	@Override
	public void writeBoolean(boolean value) throws IOException {
		this.output.writeBoolean(value);
	}

	@Override
	public void writeObject(Object object) throws IOException {
		this.output.writeObject(object);
	}

	@Override
	public void writeString(String value) throws IOException {
		this.output.writeString(value);
	}

	@Override
	public void writeBytes(byte[] buffer) throws IOException {
		this.output.writeBytes(buffer);
	}

	@Override
	public void writeInt(int value) throws IOException {
		this.output.writeInt(value);
	}

	public void close() throws IOException {
		this.output.close();
	}

	public void flush() throws IOException {
		this.output.flush();
	}
}
