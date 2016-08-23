package com.kepler.generic.wrap.arg;

import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerGenericException;
import com.kepler.generic.wrap.GenericArg;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * 对象转换
 * 
 * @author KimShen
 *
 */
public class ObjectArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	/**
	 * 数据源
	 */
	private final Map<String, Object> args;

	/**
	 * 对象类型
	 */
	private final String clazz;

	/**
	 * 指定目标类型
	 * 
	 * @param clazz
	 */
	public ObjectArg(String clazz) {
		super();
		this.args = new HashMap<String, Object>();
		this.clazz = clazz;
	}

	public ObjectArg(String clazz, Map<String, Object> args) {
		super();
		this.clazz = clazz;
		this.args = args;
	}

	public ObjectArg put(String field, Object value) {
		this.args.put(field, value);
		return this;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		try {
			Object expect = Class.forName(this.clazz).newInstance();
			// 对数据源所有数据进行SetMethod操作
			for (String field : this.args.keySet()) {
				Object value = this.args.get(field);
				// 如果为GenericArg则尝试转换否则直接赋值
				MethodUtils.invokeMethod(expect, "set" + StringUtils.capitalize(field), GenericArg.class.isAssignableFrom(value.getClass()) ? GenericArg.class.cast(value).arg() : value);
			}
			return expect;
		} catch (Exception e) {
			// 如果不为KeplerGenericException则抛出
			throw KeplerGenericException.class.isAssignableFrom(e.getClass()) ? KeplerGenericException.class.cast(e) : new KeplerGenericException(e);
		}
	}
}
