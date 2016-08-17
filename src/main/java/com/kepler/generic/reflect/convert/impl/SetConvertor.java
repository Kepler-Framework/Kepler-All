package com.kepler.generic.reflect.convert.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.kepler.generic.reflect.convert.Getter;

/**
 * 
 * @author KimShen
 *
 */
public class SetConvertor extends CollectionConvertor {

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Object> collection(Class<?> expect, Getter getter) throws Exception {
		// 如果为Set或HashSet类型则使用HashSet
		return (Set.class.equals(expect) || HashSet.class.equals(expect)) ? new HashSet<Object>(getter.length()) : Set.class.cast(expect.newInstance());
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Set.class.isAssignableFrom(clazz);
	}

}