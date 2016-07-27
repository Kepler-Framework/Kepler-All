package com.kepler.generic.arg;

import java.util.HashSet;
import java.util.Set;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArg;

/**
 * Set转换
 * 
 * @author KimShen
 *
 */
public class SetArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	private final Set<ObjectArg> args;

	public SetArg() {
		super();
		this.args = new HashSet<ObjectArg>();
	}

	public SetArg(Set<ObjectArg> args) {
		super();
		this.args = args;
	}

	/**
	 * 追加元素
	 * 
	 * @param arg
	 */
	public SetArg add(ObjectArg arg) {
		this.args.add(arg);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		Set<Object> expect = new HashSet<Object>(this.args.size());
		// 转为为真实类型
		for (ObjectArg each : this.args) {
			expect.add(each.arg());
		}
		return expect;
	}

}
