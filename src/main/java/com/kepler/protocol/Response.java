package com.kepler.protocol;

import java.io.Serializable;

import com.kepler.serial.SerialID;
import com.kepler.serial.SerialResend;

/**
 * SerialID, 序列化策略
 * 
 * @author kim 2015年7月8日
 */
public interface Response extends SerialID, SerialResend, Serializable {

	public byte[] ack();

	/**
	 * 是否存在Exception
	 * 
	 * @return
	 */
	public boolean valid();

	public Object response();

	public Throwable throwable();
}
