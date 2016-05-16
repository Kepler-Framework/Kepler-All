package com.kepler.main;

/**
 * 启动前调用
 *
 * @author kim 2015年9月15日
 */
public interface Prepare {

	/**
	 * 从System读取, Prepare不依赖PropertiesUtils
	 */
	public static final String CLASS = System.getProperty(Prepare.class.getName().toLowerCase() + ".class", null);

	public void prepare() throws Exception;
}
