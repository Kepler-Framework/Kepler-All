package com.kepler.service;

/**
 * @author kim 2015年7月3日
 */
public interface Exported {

	public void export(Service service, Object instance) throws Exception;

	public void logout(Service service) throws Exception;
}
