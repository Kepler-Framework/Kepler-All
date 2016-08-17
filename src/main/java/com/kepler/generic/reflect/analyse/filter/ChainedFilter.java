package com.kepler.generic.reflect.analyse.filter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.generic.reflect.analyse.FieldsFilter;

/**
 * @author KimShen
 *
 */
public class ChainedFilter implements FieldsFilter {

	private static final Log LOGGER = LogFactory.getLog(ChainedFilter.class);

	private final List<FieldsFilter> filters;

	public ChainedFilter(List<FieldsFilter> filters) {
		super();
		this.filters = filters;
	}

	@Override
	public boolean filter(Class<?> clazz) {
		for (FieldsFilter filter : this.filters) {
			if (filter.filter(clazz)) {
				ChainedFilter.LOGGER.debug("Class: " + clazz + " was filted by " + filter.getClass());
				return true;
			}
		}
		return false;
	}
}
