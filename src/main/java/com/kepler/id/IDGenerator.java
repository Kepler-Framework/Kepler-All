package com.kepler.id;

import java.lang.reflect.Method;

import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public interface IDGenerator {

	public Integer generate(Service service, Method method);

	public Integer generate(Service service, String method);

	public String name();
}
