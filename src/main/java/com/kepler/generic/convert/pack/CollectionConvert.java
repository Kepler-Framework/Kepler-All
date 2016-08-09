package com.kepler.generic.convert.pack;

import java.util.Collection;

import com.kepler.generic.convert.Convert;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;

/**
 * 转换内置类型集合
 * 
 * @author KimShen
 *
 */
abstract class CollectionConvert implements Convert {

	@Override
	public Object convert(Object source, String extension) throws Exception {
		Collection<Object> list = this.collection();
		for (Object each : Collection.class.cast(source)) {
			// 转换类型并加入集合
			list.add(MethodUtils.invokeStaticMethod(Class.forName(extension), "valueOf", each));
		}
		return list;
	}

	abstract protected Collection<Object> collection();
}
