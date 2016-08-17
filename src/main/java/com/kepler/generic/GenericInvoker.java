package com.kepler.generic;

/**
 * 泛化执行器
 * 
 * @author KimShen
 *
 */
public interface GenericInvoker {
	
	/**
	 * 是否激活
	 * 
	 * @return
	 */
	public boolean actived();

	/**
	 * 用于标记
	 * 
	 * @return
	 */
	public GenericMarker marker();

	/**
	 * 用于代理
	 * 
	 * @return
	 */
	public GenericDelegate delegate();
}
