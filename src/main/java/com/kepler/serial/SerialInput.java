package com.kepler.serial;

import java.io.InputStream;

/**
 * @author kim 2015年7月8日
 */
public interface SerialInput extends SerialID, SerialName {

	public <T> T input(byte[] data, Class<T> clazz) throws Exception;

	/**
	 * @param clazz
	 * @param input
	 * @param buffer 建议缓存大小
	 * @return
	 * @throws Exception
	 */
	public <T> T input(InputStream input, int buffer, Class<T> clazz) throws Exception;
}
