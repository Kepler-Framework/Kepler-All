package com.kepler.ack;

/**
 * Ack集合
 * 
 * @author KimShen
 *
 */
public interface Acks {

	/**
	 * 移除ACK
	 * 
	 * @param ack
	 * @return
	 */
	public Ack remove(byte[] ack);
}
