package com.kepler.serial.hessian;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.kepler.com.caucho.hessian.io.Hessian2Input;
import com.kepler.com.caucho.hessian.io.SerializerFactory;

/**
 * @author KimShen
 *
 */
public class HessianInputProxy2 implements HessianInputProxy {

	private final Hessian2Input input;

	public HessianInputProxy2(InputStream stream, int buffer) {
		this.input = new Hessian2Input(new BufferedInputStream(stream, buffer));
	}

	@Override
	public HessianInputProxy2 setSerializerFactory(SerializerFactory factory) {
		this.input.setSerializerFactory(factory);
		return this;
	}

	@Override
	public HessianInputProxy2 close() throws IOException {
		this.input.close();
		return this;
	}

	public boolean readBoolean() throws IOException {
		return this.input.readBoolean();
	}

	@Override
	public String readString() throws IOException {
		return this.input.readString();
	}

	@Override
	public Object readObject() throws IOException {
		return this.input.readObject();
	}

	@Override
	public byte[] readBytes() throws IOException {
		return this.input.readBytes();
	}

	public int readInt() throws IOException {
		return this.input.readInt();
	}
}
