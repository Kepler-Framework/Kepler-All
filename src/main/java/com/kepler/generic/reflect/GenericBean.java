package com.kepler.generic.reflect;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author KimShen
 *
 */
public interface GenericBean extends Serializable {

	public Object get(String key);
	
	public Long getAsLong(String key);
	
	public Long getAsLong(String key, Long def);

	public String getAsString(String key);
	
	public String getAsString(String key, String def);

	public Double getAsDouble(String key);
	
	public Double getAsDouble(String key, Double def);

	public Integer getAsInteger(String key);
	
	public Integer getAsInteger(String key, Integer def);

	public Boolean getAsBoolean(String key);
	
	public Boolean getAsBoolean(String key, Boolean def);
	
	public GenericBean getAsBean(String key);

	public GenericBean put(String key, Object value);
	
	public GenericBean mapping(Map<String, String> mapping);

	/**
	 * 获取实际参数
	 * 
	 * @return
	 */
	public LinkedHashMap<String, Object> args();
}
