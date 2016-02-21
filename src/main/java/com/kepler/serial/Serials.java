package com.kepler.serial;

/**
 * @author kim 2016年2月1日
 */
public interface Serials {

	public SerialInput input(byte serial);

	public SerialOutput output(byte serial);

	/**
	 * 默认Input
	 * 
	 * @return
	 */
	public SerialInput def4input();

	public SerialOutput def4output();

	/**
	 * 名称转换为逻辑编号
	 * 
	 * @param name
	 * @return
	 */
	public byte input(String name);

	public byte output(String name);
}
