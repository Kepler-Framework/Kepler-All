package com.kepler.method;

import java.lang.reflect.Method;

/**
 * 方法获取
 * 
 * @author KimShen
 *
 */
public interface Methods {

	/**
	 * @param service 服务名称
	 * @param method 服务方法
	 * @param parameter 请求参数
	 * @return 对应本地方法
	 * @throws Exception
	 */
	public Method method(Class<?> service, String method, Class<?>[] parameter) throws Exception;
}
