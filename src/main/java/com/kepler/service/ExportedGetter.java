package com.kepler.service;

import org.springframework.context.ApplicationContext;

/**
 * @author KimShen
 *
 */
public interface ExportedGetter {

	public Object get(Object bean, Object name, ApplicationContext context);
}
