package com.kepler.token;

import com.kepler.KeplerValidateException;
import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年2月18日
 */
public interface TokenContext {

	/**
	 * 追加Token
	 * 
	 * @param request
	 * @param token
	 * @return
	 */
	public Request set(Request request, String token);

	/**
	 * 校验Token
	 * 
	 * @param request
	 * @param token
	 * @return
	 * @throws KeplerValidateException
	 */
	public Request valid(Request request, String token) throws KeplerValidateException;
}
