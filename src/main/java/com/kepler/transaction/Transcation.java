package com.kepler.transaction;

/**
 * 事务
 * 
 * @author KimShen
 *
 */
public interface Transcation {

	/**
	 * @param uuid 事务编号
	 * @param args 事务相关参数
	 * @throws Exception
	 */
	public void transcation(String uuid, Object... args) throws Exception;
}
