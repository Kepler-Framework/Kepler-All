package com.kepler.id;

import java.lang.reflect.Method;

import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public interface IDGenerators {

	public IDGenerator get(Service service, Method method);
	
	public IDGenerator get(Service service, String method);

}
