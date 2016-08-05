package com.kepler.generic.convert.wrap;

import java.util.Collection;
import java.util.Map;

import com.kepler.generic.GenericArgs;
import com.kepler.generic.arg.ObjectArg;
import com.kepler.generic.convert.Convert;

/**
 * @author KimShen
 *
 */
abstract class ArgsConvert implements Convert {

	@SuppressWarnings("unchecked")
	@Override
	public Object convert(Object source, String extension) throws Exception {
		// 获取集合并转换为ObjectArg后Put
		GenericArgs args = this.args(extension);
		// 转换为集合并迭代转换为ObjectArg
		for (Object each : Collection.class.cast(source)) {
			args.put(new ObjectArg(extension, Map.class.cast(each)));
		}
		return args;
	}

	abstract protected GenericArgs args(String extension);
}
