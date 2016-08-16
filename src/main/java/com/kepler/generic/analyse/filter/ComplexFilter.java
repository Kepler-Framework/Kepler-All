package com.kepler.generic.analyse.filter;

import java.util.Collection;
import java.util.Map;

import com.kepler.generic.analyse.FieldsFilter;

/**
 * 集合,数组,Map过滤
 * 
 * @author KimShen
 *
 */
public class ComplexFilter implements FieldsFilter {

	@Override
	public boolean filter(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
	}
}
