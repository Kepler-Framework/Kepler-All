package com.kepler.quality;

/**
 * 统计当前正在执行的请求数量
 * 
 * @author kim
 */
public interface Quality {

	/**
	 * 闲置
	 */
	public void idle();

	/**
	 * 熔断
	 */
	public void breaking();

	/**
	 * 降级
	 */
	public void demoting();

	/**
	 * 等待
	 * 
	 * @param waiting
	 */
	public void waiting(long waiting);

	/**
	 * 累计熔断(重置内置计数)
	 * 
	 * @return
	 */
	public long getBreakingAndReset();

	/**
	 * 累计降级(重置内置计数)
	 * 
	 * @return
	 */
	public long getDemotingAndReset();

	/**
	 * 累计等待(重置内置计数)
	 * 
	 * @return
	 */
	public long getWaitingAndReset();

	/**
	 * 累计闲置(重置内置计数)
	 * 
	 * @return
	 */
	public long getIdleAndReset();
}
