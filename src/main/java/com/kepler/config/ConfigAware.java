package com.kepler.config;

import java.util.Map;

/**
 * 配置更新
 * 
 * @author zhangjiehao 2015年12月30日
 */
public interface ConfigAware {

	/**
	 * @param current 当前配置
	 * @param newconfig 新配置
	 */
	public void changed(Map<String, String> current, Map<String, String> newconfig);

}
