package com.kepler.generic;

/**
 * 泛型执行代理
 * 
 * @author KimShen
 *
 */
public interface GenericDelegate {

	/**
	 * 代理调用
	 * 
	 * @param service 指定服务
	 * @param method 指定方法
	 * @param args 参数代理集合
	 * @return 执行结果
	 * @throws Throwable
	 */
	public Object delegate(Object service, String method, GenericArgs args) throws Throwable;
}
