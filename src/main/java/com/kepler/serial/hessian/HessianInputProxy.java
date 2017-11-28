package com.kepler.serial.hessian;

import java.io.IOException;

import com.kepler.com.caucho.hessian.io.SerializerFactory;

/**
 * @author KimShen
 *
 */
public interface HessianInputProxy {

	public HessianInputProxy setSerializerFactory(SerializerFactory factory);

	public HessianInputProxy close() throws IOException;

	public boolean readBoolean() throws IOException;

	public String readString() throws IOException;

	public Object readObject() throws IOException;

	public byte[] readBytes() throws IOException;

	public int readInt() throws IOException;
}
