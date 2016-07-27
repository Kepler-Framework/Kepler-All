package com.kepler.generic;

import com.kepler.header.Headers;

/**
 * 泛型代理标记
 * 
 * @author KimShen
 *
 */
public interface GenericMarker {

	/**
	 * 标记使用泛型
	 * 
	 * @param headers
	 * @return
	 */
	public Headers mark(Headers headers);
	
	/**
	 * 是否使用泛型(隐式操作, remove相关Header)
	 * 
	 * @param headers
	 * @return
	 */
	public boolean marked(Headers headers);
}
