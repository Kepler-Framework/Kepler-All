package com.kepler.generic.reflect.analyse.filter;

import java.util.Collection;
import java.util.Map;

import com.kepler.generic.reflect.analyse.FieldsFilter;

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
