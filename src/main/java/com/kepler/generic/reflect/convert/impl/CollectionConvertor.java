package com.kepler.generic.reflect.convert.impl;

import java.util.Collection;

import org.springframework.util.Assert;

import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;
import com.kepler.generic.reflect.convert.Getter;

/**
 * @author KimShen
 *
 */
abstract class CollectionConvertor extends ComplexConvertor {

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		Getter getter = super.getter(source);
		// Guard case1, 空集合并且集合类型兼容
		if (getter.empty() && expect.isAssignableFrom(source.getClass())) {
			return source;
		}
		return this.convert(analyser, extension, this.collection(expect, getter), getter);
	}

	private Object convert(FieldsAnalyser analyser, Class<?>[] extension, Collection<Object> actual, Getter source) throws Exception {
		Assert.notNull(extension[0], "Undefined type of element");
		// 获取List元素类型对应Fields
		Fields fields = analyser.get(extension[0]);
		for (int index = 0; index < source.length(); index++) {
			actual.add(fields.actual(source.next()));
		}
		return actual;
	}

	public int sort() {
		return ConvertorPriority.DEFAULT.priority();
	}

	/**
	 * 创建集合
	 * 
	 * @param expect 预期类型
	 * @param getter 
	 * @return
	 */
	abstract protected Collection<Object> collection(Class<?> expect, Getter getter) throws Exception;
}
