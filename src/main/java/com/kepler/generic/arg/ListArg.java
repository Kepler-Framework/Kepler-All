package com.kepler.generic.arg;

import java.util.ArrayList;
import java.util.List;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArg;
import com.kepler.generic.GenericArgs;

/**
 * List转换
 * 
 * @author KimShen
 *
 */
public class ListArg extends ArrayList<ObjectArg> implements GenericArg, GenericArgs {

	private static final long serialVersionUID = 1L;

	public ListArg() {
		super();
	}

	public ListArg(List<ObjectArg> args) {
		super();
		super.addAll(args);
	}

	/**
	 * 追加元素
	 * 
	 * @param arg
	 */
	public ListArg put(ObjectArg arg) {
		super.add(arg);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		List<Object> expect = new ArrayList<Object>(super.size());
		// 转为为真实类型
		for (ObjectArg each : this) {
			expect.add(each.arg());
		}
		return expect;
	}
}
