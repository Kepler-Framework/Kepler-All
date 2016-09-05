package com.kepler.trace;

import java.io.Serializable;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface TraceCause extends Serializable {

	/**
	 * 造成异常的服务
	 * 
	 * @return
	 */
	public Service service();

	/**
	 * 造成异常的方法
	 * 
	 * @return
	 */
	public String method();

	public String trace();
}
