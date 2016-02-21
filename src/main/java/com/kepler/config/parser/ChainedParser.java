package com.kepler.config.parser;

import java.util.ArrayList;
import java.util.List;

import com.kepler.config.ConfigParser;
import com.kepler.extension.Extension;

/**
 * @author kim 2016年1月5日
 */
public class ChainedParser implements Extension, ConfigParser {

	private final List<ConfigParser> parsers = new ArrayList<ConfigParser>();

	@Override
	public Object parse(Class<?> request, String config) {
		for (ConfigParser each : this.parsers) {
			if (each.support(request)) {
				return each.parse(request, config);
			}
		}
		return null;
	}

	public boolean support(Class<?> request) {
		for (ConfigParser each : this.parsers) {
			if (each.support(request)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ChainedParser install(Object instance) {
		this.parsers.add(ConfigParser.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return ConfigParser.class;
	}
}
