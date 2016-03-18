package com.kepler.connection;

/**
 * 统计当前正在执行的请求数量
 * 
 * @author kim
 *
 * 2016年3月18日
 */
public interface Counter {

	public void incr();

	public void decr();

	public long remain();
}
