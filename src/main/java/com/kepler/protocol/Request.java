package com.kepler.protocol;

import java.io.Serializable;

import com.kepler.header.Headers;
import com.kepler.serial.SerialID;
import com.kepler.service.Service;

/**
 * SerialID, 序列化策略
 * 
 * @author kim 2015年7月8日
 */
public interface Request extends SerialID, Serializable {

	public Service service();

	public String method();
	
	public byte[] ack();

	/**
	 * 实际参数
	 * 
	 * @return
	 */
	public Object[] args();

	/**
	 * 是否为异步调用
	 * 
	 * @return
	 */
	public boolean async();
	
	public Headers headers();
	
	/**
	 * 精确参数类型
	 * 
	 * @return
	 */
	public Class<?>[] types();

	/**
	 * 快捷代理
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key);

	public String get(String key, String def);

	public Request put(String key, String value);

	public Request putIfAbsent(String key, String value);
	
	public Request put(String key, Object value);

	public Request putIfAbsent(String key, Object value);
}
