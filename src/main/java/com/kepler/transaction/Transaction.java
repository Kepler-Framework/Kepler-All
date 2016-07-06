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
	 * @param location 同步执行器
	 * @return 是否执行成功, 如果为False则异步触发回滚逻辑
 	 * @throws Exception 执行异常
	 */
	public Object commit(Request request, Invoker invoker) throws Exception;
}
