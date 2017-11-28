package com.kepler.serial.hessian;

import java.io.IOException;

import com.kepler.com.caucho.hessian.io.SerializerFactory;

/**
 * @author KimShen
 *
 */
public interface HessianOutputProxy {

	public HessianOutputProxy setSerializerFactory(SerializerFactory factory);

	public void writeBoolean(boolean value) throws IOException;

	public void writeObject(Object object) throws IOException;

	public void writeString(String value) throws IOException;

	public void writeBytes(byte[] buffer) throws IOException;

	public void writeInt(int value) throws IOException;

	public void close() throws IOException;

	public void flush() throws IOException;
}
