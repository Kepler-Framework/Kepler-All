package com.kepler.protocol;

import com.kepler.serial.SerialID;

/**
 * @author kim 2015年7月8日
 */
public interface ResponseFactory extends SerialID {

	/**
	 * @param ack
	 * @param response
	 * @param serial 序列化策略
	 * @return
	 */
	public Response response(byte[] ack, Object response, byte serial);

	public Response throwable(byte[] ack, Throwable throwable, byte serial);

}
