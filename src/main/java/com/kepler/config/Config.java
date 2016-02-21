package com.kepler.config;

import java.util.Map;

/**
 * @author kim 2015年12月27日
 */
public interface Config {

	/**
	 * 新配置回调
	 * 
	 * @param configs
	 */
	public void config(Map<String, String> configs);
}
