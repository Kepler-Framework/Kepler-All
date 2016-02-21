package com.kepler.invoker.forkjoin;

/**
 * @author kim 2016年1月16日
 */
public interface Joiner {

	/**
	 * @param current 当前值
	 * @param joined 需要合并值
	 * @return
	 */
	public Object join(Object current, Object joined);

	public String name();
}
