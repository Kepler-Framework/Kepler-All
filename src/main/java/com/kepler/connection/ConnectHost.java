package com.kepler.connection;

import java.util.concurrent.Delayed;

import com.kepler.host.Host;

/**
 * 延迟重连Host
 * 
 * @author kim 2015年7月11日
 */
public interface ConnectHost extends Delayed {

	public Host host();
}
