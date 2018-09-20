package com.kepler.mock;

import org.springframework.core.Ordered;

import com.kepler.service.Service;

/**
 * @author kim 2016年1月13日
 */
public interface MockerContext extends Ordered {

	public Mocker get(Service service);
}
