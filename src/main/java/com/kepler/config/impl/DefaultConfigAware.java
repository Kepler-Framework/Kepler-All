package com.kepler.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.ConfigAware;
import com.kepler.extension.Extension;

/**
 * @author zhangjiehao 2015年12月30日
 */
public class DefaultConfigAware implements ConfigAware, Extension {

	private static final Log LOGGER = LogFactory.getLog(DefaultConfigAware.class);

	private final List<ConfigAware> awares = new ArrayList<ConfigAware>();

	@Override
	public void changed(Map<String, String> current, Map<String, String> newconfig) {
		for (ConfigAware each : this.awares) {
			try {
				each.changed(current, newconfig);
			} catch (Throwable e) {
				DefaultConfigAware.LOGGER.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public DefaultConfigAware install(Object instance) {
		this.awares.add(ConfigAware.class.cast(instance));
		return this;
	}

	@Override
	public Class<?> interested() {
		return ConfigAware.class;
	}
}
