package com.kepler.traffic;

/**
 * 流量
 * 
 * @author kim 2016年1月7日
 */
public interface Traffic {

	public void input(long bytes);

	public void output(long bytes);

	public long getInputAndReset();

	public long getOutputAndReset();
}
