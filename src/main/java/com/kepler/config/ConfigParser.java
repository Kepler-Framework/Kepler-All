package com.kepler.config;

/**
 * 配置解析(String to Object)
 * 
 * @author kim 2016年1月5日
 */
public interface ConfigParser {

	/**
	 * 解析String为指定对象 
	 * @param request 指定对象类型
	 * @param config
	 * @return
	 */
	public Object parse(Class<?> request, String config);

	/**
	 * 是否支持该Class的转换
	 * 
	 * @param request
	 * @return
	 */
	public boolean support(Class<?> request);
}
