package com.kepler.generic;

import com.kepler.service.Service;

/**
 * 泛化调用代理
 * 
 * @author KimShen
 *
 */
public interface GenericService {

	/**
	 * 加载服务
	 * 
	 * @param service
	 * @throws Exception
	 */
	public void imported(Service service) throws Exception;

	/**
	 * 代理泛化
	 * 
	 * @param service 泛化服务
	 * @param method 指定方法
	 * @param classes 参数类型
	 * @param args 参数代理集合
	 * @return 执行结果
	 * @throws Throwable
	 */
	public Object invoke(Service service, String method, String[] classes, Object... args) throws Throwable;
}
