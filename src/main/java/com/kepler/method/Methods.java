package com.kepler.method;

/**
 * 方法获取
 * 
 * @author KimShen
 *
 */
public interface Methods {

	/**
	 * @param 按参数类型查找
	 * 
	 * @throws Exception
	 */
	public MethodInfo method(Class<? extends Object> service, String method, Class<?>[] classes) throws Exception;

	/**
	 * @param 按参数名称查找
	 * 
	 * @throws Exception
	 */
	public MethodInfo method(Class<? extends Object> service, String method, String[] names) throws Exception;

	/**
	 * @param 按参数长度查找
	 * 
	 * @throws Exception
	 */
	public MethodInfo method(Class<? extends Object> service, String method, int size) throws Exception;
}
