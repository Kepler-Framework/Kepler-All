package com.kepler.generic;

/**
 * @author KimShen
 *
 */
public interface GenericResponseFactory {

	/**
	 * 获取有效泛化结果
	 * 
	 * @param response
	 * @return
	 */
	public GenericResponse response(Object response);

	/**
	 * 获取无效泛化结果
	 * 
	 * @return
	 */
	public GenericResponse unvalid();
}
