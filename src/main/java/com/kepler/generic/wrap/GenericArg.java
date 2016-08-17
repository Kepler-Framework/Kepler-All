package com.kepler.generic.wrap;

import java.io.Serializable;

import com.kepler.KeplerGenericException;

/**
 * 代理参数
 * 
 * @author KimShen
 *
 */
public interface GenericArg extends Serializable {

	/**
	 * 获取实际类型
	 * 
	 * @return
	 */
	public Object arg() throws KeplerGenericException;
}
