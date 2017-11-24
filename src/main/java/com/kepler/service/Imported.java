package com.kepler.service;

/**
 * @author kim 2015年7月10日
 */
public interface Imported {

	public void subscribe(Service service) throws Exception;

	public void unsubscribe(Service service) throws Exception;
}
