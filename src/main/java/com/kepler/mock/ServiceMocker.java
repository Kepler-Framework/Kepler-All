package com.kepler.mock;


/**
 * @author kim 2016年1月13日
 */
public interface ServiceMocker extends Mocker{

	/**
	 * Mock支持的Service
	 * 
	 * @return
	 */
	public Class<?> support();
}
