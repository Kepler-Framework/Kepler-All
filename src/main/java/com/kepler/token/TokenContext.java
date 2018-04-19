package com.kepler.token;

import com.kepler.KeplerValidateException;
import com.kepler.channel.ChannelInvoker;
import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年2月18日
 */
public interface TokenContext {

	/**
	 * 是否激活
	 * 
	 * @return
	 */
	public boolean actived();

	/**
	 * 追加Token
	 * 
	 * @param request
	 * @param invoker
	 * @return
	 */
	public void set(Request request, ChannelInvoker invoker);

	/**
	 * 校验Token
	 * 
	 * @param request
	 * @return
	 * @throws KeplerValidateException
	 */
	public void valid(Request request) throws KeplerValidateException;
}
