package com.kepler.generic.reflect.convert.impl;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.ConvertorPriority;
import com.kepler.org.apache.commons.lang.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author KimShen
 *
 */
public class DateConvertor extends SimpleConvertor {

	/**
	 * 日期格式化
	 */
	private static final String FORMAT = PropertiesUtils.get(DateConvertor.class.getName().toLowerCase() + ".format", "yyyy-MM-dd HH:mm:ss");

	@Override
	public Object doConvert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
		// 如果Source为Long则使用new Date(long), 否则使用字符串格式化
		if (source.getClass() == Long.class || source.getClass() == long.class) {
			return new Date(Long.class.cast(source));
		} else if (source.getClass() == String.class && NumberUtils.isNumber((String)source)) {
			return new Date(Long.parseLong((String) source));
		} else {
			return new SimpleDateFormat(DateConvertor.FORMAT).parse(source.toString());
		}
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Date.class.equals(clazz);
	}

	public int sort() {
		return ConvertorPriority.HIGH.priority();
	}
}
