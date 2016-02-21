package com.kepler.main;

import com.kepler.config.PropertiesUtils;

/**
 * 启动前调用
 * 
 * @author kim 2015年9月15日
 */
public interface Prepare {

	public final static String CLASS = PropertiesUtils.get(Prepare.class.getName().toLowerCase() + ".class");

	public void prepare() throws Exception;
}
