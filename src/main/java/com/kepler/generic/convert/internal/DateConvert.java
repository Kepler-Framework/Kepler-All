package com.kepler.generic.convert.internal;

import java.text.SimpleDateFormat;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.convert.Convert;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public class DateConvert implements Convert {

	/**
	 * 日期格式化
	 */
	private static final String FORMAT = PropertiesUtils.get(DateConvert.class.getName().toLowerCase() + ".format", "yyyy-MM-dd hh:mm:ss");

	private static final String NAME = "date";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		// 存在扩展则使用扩展
		return new SimpleDateFormat(StringUtils.defaultIfEmpty(extension, DateConvert.FORMAT)).parse(source.toString());
	}

	@Override
	public String name() {
		return DateConvert.NAME;
	}
}
