package com.kepler.id;

import com.kepler.protocol.Bytes;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public interface IDGenerator {

	public Bytes generate();

	public String name();
}
