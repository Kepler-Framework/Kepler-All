package com.kepler.generic.wrap.arg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.kepler.KeplerGenericException;
import com.kepler.generic.wrap.GenericArg;
import com.kepler.generic.wrap.GenericArgs;

/**
 * 数组转换
 * 
 * @author KimShen
 *
 */
public class ArrayArg implements GenericArg, GenericArgs {

	private static final long serialVersionUID = 1L;

	private final List<ObjectArg> args = new ArrayList<ObjectArg>();

	private final String clazz;

	public ArrayArg(String clazz) {
		super();
		this.clazz = clazz;
	}

	/**
	 * 追加元素
	 * 
	 * @param arg
	 */
	public ArrayArg put(ObjectArg arg) {
		this.args.add(arg);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		try {
			Object[] expect = Object[].class.cast(Array.newInstance(Class.forName(this.clazz), this.args.size()));
			// 转为为真实类型
			for (int index = 0; index < this.args.size(); index++) {
				expect[index] = this.args.get(index).arg();
			}
			return expect;
		} catch (Exception e) {
			// 如果不为KeplerGenericException则抛出
			throw KeplerGenericException.class.isAssignableFrom(e.getClass()) ? KeplerGenericException.class.cast(e) : new KeplerGenericException(e);
		}
	}
}
