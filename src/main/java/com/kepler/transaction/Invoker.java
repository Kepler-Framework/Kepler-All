package com.kepler.transaction;

/**
 * @author KimShen
 *
 */
public interface Invoker {

	/**
	 * @param uuid 事务ID
	 * @param args 实际参数
	 * @return 执行结果
	 * @throws Exception
	 */
	public Object invoke(String uuid, Object... args) throws Exception;
}
