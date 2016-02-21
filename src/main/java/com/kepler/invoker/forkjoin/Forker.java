package com.kepler.invoker.forkjoin;

/**
 * @author kim 2016年1月18日
 */
public interface Forker {

	/**
	 * Args拆分
	 * 
	 * @param args
	 * @param tag 标签
	 * @param index 
	 * @return
	 */
	public Object[] fork(Object[] args, String tag, int index);

	public String name();
}
