package com.kepler.protocol;

import com.kepler.KeplerValidateException;

/**
 * Request参数合法性校验
 * 
 * @author kim 2015年10月30日
 */
public interface RequestValidation {

	public Request valid(Request request) throws KeplerValidateException;

	public Scope scope();

	/**
	 * 作用域
	 * 
	 * @author kim
	 *
	 * 2016年2月18日
	 */
	public enum Scope {

		ALL, CLIENT, SERVICE;
	}
}
