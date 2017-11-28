package com.kepler.protocol;

/**
 * @author KimShen
 *
 */
public interface Protocols {

	/**
	 * 获取Response/Request在指定序列化协议中的Class
	 * 
	 * @param serial
	 * @param protocol
	 * @return
	 */
	public Class<?> protocol(byte serial);
}
