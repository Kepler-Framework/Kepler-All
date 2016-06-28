package com.kepler.transaction;

/**
 * 事务上下文
 * 
 * @author KimShen
 *
 */
public interface TranscationContext {

	/**
	 * 提交事务
	 * 
	 * @param request 事务请求
	 * @return 是否执行成功, 如果为False则异步触发回滚逻辑
	 */
	public boolean commit(TranscationRequest request);
}
