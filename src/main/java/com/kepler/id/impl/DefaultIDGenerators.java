package com.kepler.id.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.extension.Extension;
import com.kepler.id.IDGenerator;
import com.kepler.id.IDGenerators;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public class DefaultIDGenerators implements IDGenerators, Extension {

	private final Profile profile;
	
	private final Map<String, IDGenerator> ids = new HashMap<String, IDGenerator>();
	
	private static final String GENERATOR_KEY = DefaultIDGenerators.class.getName().toLowerCase() + ".generator";

	private static final String GENERATOR_DEF = PropertiesUtils.get(DefaultIDGenerators.GENERATOR_KEY, IncrGenerator.NAME);

	public DefaultIDGenerators(Profile profile) {
		this.profile = profile;
	}
	
	@Override
	public IDGenerator get(Service service, Method method) {
		return get(service, method.getName());
	}

	@Override
	public IDGenerator get(Service service, String method) {
		String generatorName = PropertiesUtils.profile(this.profile.profile(service), GENERATOR_KEY, GENERATOR_DEF);
		if (!ids.containsKey(generatorName)) {
			throw new KeplerException("加载错误。找不到可用的ID generator");
		}
		return this.ids.get(PropertiesUtils.profile(this.profile.profile(service), GENERATOR_KEY, GENERATOR_DEF));
	}
	
	@Override
	public Extension install(Object instance) {
		IDGenerator generator = (IDGenerator)instance; 
		this.ids.put(generator.name(), generator);
		return this;
	}

	@Override
	public Class<?> interested() {
		return IDGenerator.class;
	}

}
