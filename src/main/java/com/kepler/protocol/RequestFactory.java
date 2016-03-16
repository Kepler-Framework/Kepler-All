package com.kepler.protocol;

import java.lang.reflect.Method;

import com.kepler.header.Headers;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public interface RequestFactory {

	/**
	 * @param headers
	 * @param service
	 * @param method
	 * @param async
	 * @param args
	 * @param types
	 * @param ack
	 * @param serial 序列化策略
	 * @return
	 */
	public Request request(Headers headers, Service service, String method, boolean async, Object[] args, Class<?>[] types, byte[] ack, byte serial);

	public Request request(Headers headers, Service service, Method method, boolean async, Object[] args, byte[] ack, byte serial);

	/**
	 * 复制Request参数并替换Args
	 * 
	 * @param request
	 * @param args
	 * @return
	 */
	public Request request(Request request, byte[] ack, Object[] args);

	/**
	 * 复制Request参数并替换Async
	 * 
	 * @param request
	 * @param ack
	 * @return
	 */
	public Request request(Request request, byte[] ack, boolean async);

	/**
	 * 复制Request参数并替换Ack
	 * 
	 * @param request
	 * @param ack
	 * @return
	 */
	public Request request(Request request, byte[] ack);
}
