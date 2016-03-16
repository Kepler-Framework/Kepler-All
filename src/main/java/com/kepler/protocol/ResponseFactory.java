package com.kepler.protocol;

/**
 * @author kim 2015年7月8日
 */
public interface ResponseFactory {

	/**
	 * @param ack
	 * @param response
	 * @param serial 序列化策略
	 * @return
	 */
	public Response response(byte[] ack, Object response, byte serial);

	public Response throwable(byte[] ack, Throwable throwable, byte serial);

}
