package com.kepler.invoker.forkjoin.impl;

import com.kepler.invoker.forkjoin.Forker;

/**
 * 使用相同Args
 * 
 * @author kim 2016年1月18日
 */
public class Forker4Equals implements Forker {

	public static final String NAME = "equals";

	@Override
	public Object[] fork(Object[] args, String tag, int index) {
		return args;
	}

	@Override
	public String name() {
		return Forker4Equals.NAME;
	}
}
