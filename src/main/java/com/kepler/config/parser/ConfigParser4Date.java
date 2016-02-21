package com.kepler.config.parser;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.ConfigParser;
import com.kepler.config.PropertiesUtils;

/**
 * String -> Date解析
 * 
 * @author kim 2016年1月5日
 */
public class ConfigParser4Date implements ConfigParser {

	/**
	 * 默认格式
	 */
	private final static String FORMAT = PropertiesUtils.get(ConfigParser4Date.class.getName().toLowerCase() + ".format", "yyyy-MM-dd hh:mm:ss");

	private final static Log LOGGER = LogFactory.getLog(ConfigParser4Date.class);

	@Override
	public Object parse(Class<?> request, String config) {
		try {
			return new SimpleDateFormat(ConfigParser4Date.FORMAT).parse(config);
		} catch (Throwable throwable) {
			ConfigParser4Date.LOGGER.error(throwable.getMessage(), throwable);
			return null;
		}
	}

	@Override
	public boolean support(Class<?> request) {
		return Date.class.equals(request);
	}
}
