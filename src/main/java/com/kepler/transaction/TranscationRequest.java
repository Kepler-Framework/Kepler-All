package com.kepler.transaction;

import java.io.Serializable;

/**
 * 事务请求
 * 
 * @author KimShen
 *
 */
public interface TranscationRequest extends Serializable {

	/**
	 * 事务编号
	 * 
	 * @return
	 */
	public String uuid();

	/**
	 * 请求参数
	 * 
	 * @return
	 */
	public Object[] args();

	/**
	 * 用于提交事务的策略(从ApplicationContext中查找)
	 * 
	 * @return
	 */
	public Class<? extends Transcation> main();

	/**
	 * 用于回滚事物的策略(从ApplicationContext中查找)
	 * 
	 * @return
	 */
	public Class<? extends Transcation> rollback();
}
