package com.kepler.generic;

import com.kepler.header.Headers;

/**
 * 泛化代理标记
 * 
 * @author KimShen
 *
 */
public interface GenericMarker {

	/**
	 * 标记使用泛化
	 * 
	 * @param headers
	 * @return
	 */
	public Headers mark(Headers headers);
}
