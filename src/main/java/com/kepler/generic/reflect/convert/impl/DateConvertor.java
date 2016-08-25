package com.kepler.generic.reflect.convert.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.ConvertorPriority;

/**
 * @author KimShen
 *
 */
public class DateConvertor implements Convertor {

	/**
	 * 日期格式化
	 */
	private static final String FORMAT = PropertiesUtils.get(DateConvertor.class.getName().toLowerCase() + ".format", "yyyy-MM-dd hh:mm:ss");

	@Override
	public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		// 如果Source为Long则使用new Date(long), 否则使用字符串格式化
		return (source.getClass() == Long.class || source.getClass() == long.class) ? new Date(Long.class.cast(source)) : new SimpleDateFormat(DateConvertor.FORMAT).parse(source.toString());
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Date.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
