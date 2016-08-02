package com.kepler.generic.arg;

import java.util.HashSet;
import java.util.Set;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArg;
import com.kepler.generic.GenericArgs;

/**
 * Set转换
 * 
 * @author KimShen
 *
 */
public class SetArg extends HashSet<ObjectArg> implements GenericArg, GenericArgs {

	private static final long serialVersionUID = 1L;

	public SetArg() {
		super();
	}

	public SetArg(Set<ObjectArg> args) {
		super();
		super.addAll(args);
	}

	/**
	 * 追加元素
	 * 
	 * @param arg
	 */
	public SetArg put(ObjectArg arg) {
		super.add(arg);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		Set<Object> expect = new HashSet<Object>(super.size());
		// 转为为真实类型
		for (ObjectArg each : this) {
			expect.add(each.arg());
		}
		return expect;
	}

}
