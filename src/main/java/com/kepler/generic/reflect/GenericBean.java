package com.kepler.generic.reflect;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author KimShen
 *
 */
public interface GenericBean extends Serializable {

	/**
	 * 获取实际参数
	 * 
	 * @return
	 */
	public LinkedHashMap<String, Object> args();
}
