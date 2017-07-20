package com.kepler.serial;

import java.io.OutputStream;

import com.kepler.KeplerSerialException;

/**
 * @author kim 2015年10月13日
 */
public interface SerialOutput extends SerialID, SerialName {

	public byte[] output(Object data, Class<?> clazz) throws KeplerSerialException;

	public void output(Object data, Class<?> clazz, OutputStream output, int buffer) throws KeplerSerialException;
}
