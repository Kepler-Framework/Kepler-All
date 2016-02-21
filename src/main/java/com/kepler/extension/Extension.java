package com.kepler.extension;

/**
 * @author kim 2015年7月14日
 */
public interface Extension {

	/**
	 * 安装扩展点
	 * 
	 * @param instance
	 * @return
	 */
	public Extension install(Object instance);

	public Class<?> interested();
}
