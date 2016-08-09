package com.kepler.generic.convert.pack;

import java.lang.reflect.Array;
import java.util.Collection;

import com.kepler.generic.convert.Convert;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * 基础类型数组
 * 
 * @author KimShen
 *
 */
public class ArrayConvert implements Convert {

	private static final String NAME = "primary-array";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		// 尝试转换为集合
		Collection<?> collection = Collection.class.cast(source);
		// 创建对应长度数组
		Object[] expect = Object[].class.cast(Array.newInstance(Class.forName(extension), collection.size()));
		// 数组索引位
		int index = 0;
		for (Object each : collection) {
			expect[index++] = MethodUtils.invokeStaticMethod(Class.forName(extension), "valueOf", each);
		}
		return expect;
	}

	@Override
	public String name() {
		return ArrayConvert.NAME;
	}

}
