package com.kepler.serial;

/**
 * @author kim 2016年2月2日
 */
public interface SerialName {

	public boolean actived();
	
	/**
	 * 序列化策略名称
	 * 
	 * @return
	 */
	public String name();
}
