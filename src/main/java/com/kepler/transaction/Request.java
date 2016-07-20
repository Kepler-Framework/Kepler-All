package com.kepler.transaction;

import java.io.Serializable;

import com.kepler.header.Headers;

/**
 * 事务请求
 * 
 * @author KimShen
 *
 */
public interface Request extends Serializable {

	/**
	 * 获取事务编号
	 * 
	 * @return
	 */
	public String uuid();

	/**
	 * 获取请求参数
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

	/**
	 * 获取Header上下文
	 * 
	 * @return
	 */
	public Headers headers();

	/**
	 * 指定Headers上下文(回调)
	 * 
	 * @param headers
	 * @return
	 */
	public Request headers(Headers headers);
}
