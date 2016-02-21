package com.kepler.mock;

import com.kepler.service.Service;

/**
 * @author kim 2016年1月13日
 */
public interface MockerContext {

	public Mocker get(Service service);
}
