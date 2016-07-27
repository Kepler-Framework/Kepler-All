package com.kepler.generic.arg;

import java.util.ArrayList;
import java.util.List;

import com.kepler.KeplerGenericException;
import com.kepler.generic.GenericArg;

/**
 * List转换
 * 
 * @author KimShen
 *
 */
public class ListArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	private final List<ObjectArg> args;

	public ListArg() {
		super();
		this.args = new ArrayList<ObjectArg>();
	}

	public ListArg(List<ObjectArg> args) {
		super();
		this.args = args;
	}

	/**
	 * 追加元素
	 * 
	 * @param arg
	 */
	public ListArg add(ObjectArg arg) {
		this.args.add(arg);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		List<Object> expect = new ArrayList<Object>(this.args.size());
		// 转为为真实类型
		for (ObjectArg each : this.args) {
			expect.add(each.arg());
		}
		return expect;
	}
}
