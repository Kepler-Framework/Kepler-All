package com.kepler.serial;

import java.io.OutputStream;

/**
 * @author kim 2015年10月13日
 */
public interface SerialOutput extends SerialID, SerialName {

	public byte[] output(Object data, Class<?> clazz) throws Exception;

	public OutputStream output(Object data, Class<?> clazz, OutputStream output, int buffer) throws Exception;
}
