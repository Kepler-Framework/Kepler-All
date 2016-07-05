package com.kepler.transaction;

import java.io.Serializable;

/**
 * 事务请求
 * 
 * @author KimShen
 *
 */
public interface Request extends Serializable {

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
	 * 回滚入口
	 * 
	 * @return
	 */
	public Location location();
}
