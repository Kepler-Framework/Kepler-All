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
	 * @return 如果返回True表示事务已经提交, 后续执行如果失败将触发回滚策略.
	 */
	public boolean commit(TranscationRequest request);
}
