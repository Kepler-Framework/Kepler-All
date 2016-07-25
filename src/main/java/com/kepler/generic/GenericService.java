package com.kepler.generic;

import com.kepler.service.Service;

/**
 * 泛型调用代理
 * 
 * @author KimShen
 *
 */
public interface GenericService {

	/**
	 * 代理泛型
	 * 
	 * @param service 泛型服务
	 * @param method 指定方法
	 * @param args 代理参数集合
	 * @return 执行结果
	 * @throws Throwable
	 */
	public Object invoke(Service service, String method, GenericArg... args) throws Throwable;
}
