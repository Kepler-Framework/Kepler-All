package com.kepler;

/**
 * 异步请求相关错误
 * 
 * @author kim 2015年8月28日
 */
public class KeplerAsyncException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;

	public KeplerAsyncException(Throwable e) {
		super(e);
	}
}
