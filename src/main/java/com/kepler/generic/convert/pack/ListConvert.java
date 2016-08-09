package com.kepler.generic.convert.pack;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 包装类型List转换
 * 
 * @author KimShen
 *
 */
public class ListConvert extends CollectionConvert {

	private static final String NAME = "primary-list";

	@Override
	protected Collection<Object> collection() {
		return new ArrayList<Object>();
	}

	@Override
	public String name() {
		return ListConvert.NAME;
	}
}
