package com.kepler.generic.convert.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.generic.analyse.Fields;
import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class MapConvertor implements Convertor {

	@Override
	@SuppressWarnings("unchecked")
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Map<Object, Object> source_convert = Map.class.cast(source);
		// Guard case, Map为空并且Map类型兼容
		if (source_convert.isEmpty() && expect.isAssignableFrom(source_convert.getClass())) {
			return source_convert;
		}
		// 如果为接口或类型为HashMap则使用HashMap
		return this.convert(analyser, extension, source_convert, (expect.isInterface() || HashMap.class.equals(expect)) ? new HashMap<Object, Object>(source_convert.size()) : Map.class.cast(expect.newInstance()));
	}

	private Object convert(FieldsAnalyser analyser, Class<?>[] extension, Map<Object, Object> source, Map<Object, Object> actual) throws Exception {
		// 分别获取Key, Value对应Fields
		Fields fields_key = analyser.get(extension[0]);
		Fields fields_value = analyser.get(extension[1]);
		for (Object key : source.keySet()) {
			actual.put(fields_key.actual(key), fields_value.actual(source.get(key)));
		}
		return actual;
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	public int sort() {
		return ConvertorPriority.DEFAULT.priority();
	}
}
