package com.kepler.config.parser;

import java.io.File;

import com.kepler.config.ConfigParser;

/**
 * String -> File
 * 
 * @author kim 2016年1月16日
 */
public class ConfigParser4File implements ConfigParser {

	@Override
	public Object parse(Class<?> request, String config) {
		return new File(config);
	}

	@Override
	public boolean support(Class<?> request) {
		return File.class.equals(request);
	}
}
