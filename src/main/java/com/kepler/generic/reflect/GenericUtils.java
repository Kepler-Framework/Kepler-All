package com.kepler.generic.reflect;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KimShen
 *
 */
abstract public class GenericUtils {

	/**
	 * 原生类型映射
	 */
	private static final Map<String, Class<?>> PRIMITIVE = new HashMap<String, Class<?>>();

	static {
		GenericUtils.PRIMITIVE.put(boolean.class.getName(), boolean.class);
		GenericUtils.PRIMITIVE.put(double.class.getName(), double.class);
		GenericUtils.PRIMITIVE.put(short.class.getName(), short.class);
		GenericUtils.PRIMITIVE.put(float.class.getName(), float.class);
		GenericUtils.PRIMITIVE.put(long.class.getName(), long.class);
		GenericUtils.PRIMITIVE.put(byte.class.getName(), byte.class);
		GenericUtils.PRIMITIVE.put(int.class.getName(), int.class);
	}

	public static boolean contains(String name) {
		return GenericUtils.PRIMITIVE.containsKey(name);
	}
	
	public static Class<?> get(String name) {
		return GenericUtils.PRIMITIVE.get(name);
	}
}
