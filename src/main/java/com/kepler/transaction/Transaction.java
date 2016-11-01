package com.kepler.transaction;

/**
 * 事务上下文
 * 
 * @author KimShen
 *
 */
public interface Transaction {

	/**
	 * 提交事务
	 * 
	 * @param request 事务请求
	 * @param invoker 执行器
	 * @return 
 	 * @throws Exception 执行异常
	 */
	public Object commit(Request request, Invoker invoker) throws Exception;
}
