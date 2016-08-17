package com.kepler.generic;

/**
 * @author KimShen
 *
 */
public interface GenericResponse {

	/**
	 * 指定Response
	 * 
	 * @param response
	 * @return
	 */
	public GenericResponse response(Object response);

	/**
	 * 获取Response
	 * 
	 * @return
	 */
	public Object response();

	/**
	 * 是否为有效Response
	 * 
	 * @return
	 */
	public boolean valid();
}
