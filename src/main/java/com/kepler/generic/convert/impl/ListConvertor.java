package com.kepler.generic.convert.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.kepler.generic.convert.Getter;

/**
 * @author KimShen
 *
 */
public class ListConvertor extends CollectionConvertor {

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Object> collection(Class<?> expect, Getter getter) throws Exception {
		// 如果为接口或类型为ArrayList类型则使用ArrayList
		return (expect.isInterface() || ArrayList.class.equals(expect)) ? new ArrayList<Object>(getter.length()) : List.class.cast(expect.newInstance());
	}
	
	@Override
	public boolean support(Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}

}
