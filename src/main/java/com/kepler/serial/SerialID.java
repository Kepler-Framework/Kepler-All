package com.kepler.serial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim
 *
 * 2016年2月11日
 */
public interface SerialID {

	public final static String SERIAL_DEF = "default";

	public final static String SERIAL_KEY = SerialID.class.getName().toLowerCase() + ".serial";

	/**
	 * 默认序列化策略
	 */
	public final static String SERIAL_VAL = PropertiesUtils.get(SerialID.SERIAL_KEY, SerialID.SERIAL_DEF);

	/**
	 * 是否允许动态化序列化策略(Profile)
	 */
	public final static boolean DYAMIC = PropertiesUtils.get(SerialID.class.getName().toLowerCase() + ".dynamic", false);

	/**
	 * 获取序列化策略
	 * 
	 * @return
	 */
	@JsonProperty
	public byte serial();
}
