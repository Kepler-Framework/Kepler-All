package com.kepler.generic.convert;

/**
 * 取值器
 * 
 * @author KimShen
 *
 */
public interface Getter {

	/**
	 * 是否为空
	 * 
	 * @return
	 */
	public boolean empty();

	/**
	 * 下一个值
	 * 
	 * @return
	 */
	public Object next();

	/**
	 * 总长度
	 * 
	 * @return
	 */
	public int length();
}