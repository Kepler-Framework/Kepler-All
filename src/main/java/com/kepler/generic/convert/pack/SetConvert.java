package com.kepler.generic.convert.pack;

import java.util.Collection;
import java.util.HashSet;

/**
 * 包装类型List转换
 * 
 * @author KimShen
 *
 */
public class SetConvert extends CollectionConvert {

	private static final String NAME = "primary-set";

	@Override
	protected Collection<Object> collection() {
		return new HashSet<Object>();
	}

	@Override
	public String name() {
		return SetConvert.NAME;
	}
}
