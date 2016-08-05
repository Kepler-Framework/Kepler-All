package com.kepler.generic.convert;

/**
 * 属性类型转换器
 * 
 * @author KimShen
 *
 */
public interface Convert {

	/**
	 * @param source 当前类型
	 * @param extension 扩展标记
	 * @return 实际类型
	 * @throws Exception
	 */
	public Object convert(Object source, String extension) throws Exception;

	/**
	 * 加载名称
	 * 
	 * @return
	 */
	public String name();
}
