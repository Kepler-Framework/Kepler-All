package com.kepler.generic.analyse.filter;

import com.kepler.generic.analyse.FieldsFilter;

/**
 * 内置类,原生类,枚举类过滤
 * 
 * @author KimShen
 *
 */
public class PrimitiveFilter implements FieldsFilter {

	@Override
	public boolean filter(Class<?> clazz) {
		return clazz.getName().startsWith("java") || clazz.isPrimitive() || clazz.isEnum();
	}
}
