package com.kepler.generic.wrap.arg;

import com.kepler.KeplerGenericException;
import com.kepler.generic.wrap.GenericArg;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * @author KimShen
 *
 */
public class EnumArg implements GenericArg {

	private static final long serialVersionUID = 1L;

	/**
	 * 对象类型
	 */
	private final String clazz;

	private final String value;

	public EnumArg(String clazz, String value) {
		super();
		this.clazz = clazz;
		this.value = value;
	}

	@Override
	public Object arg() throws KeplerGenericException {
		try {
			return MethodUtils.invokeStaticMethod(Class.forName(this.clazz), "valueOf", this.value);
		} catch (Exception e) {
			throw new KeplerGenericException(e);
		}
	}
}
