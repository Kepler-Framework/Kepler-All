package com.kepler.transaction;

import java.util.List;

/**
 * 事务持久化
 * 
 * @author KimShen
 *
 */
public interface TranscationPersistent {

	/**
	 * 获取尚未删除持久化事务请求
	 * 
	 * @return
	 */
	public List<TranscationRequest> remain();

	/**
	 * 删除持久化事务请求
	 * 
	 * @param uuid 事务号
	 * @return 是否成功删除持久化事务请求
	 */
	public boolean delete(String uuid);

	/**
	 * 持久化事务请求
	 * 
	 * @param request
	 * @return 如果持久化成功则返回True
	 */
	public boolean persistent(TranscationRequest request);
}
