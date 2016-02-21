package com.kepler.id.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.id.IDGenerator;
import com.kepler.service.Service;

/**
 * @author kim
 *
 * 2016年2月3日
 */
public class DefaultGenerator implements IDGenerator {

	private final static String NAME = "DEFAULT";

	private final static String GENERATOR_KEY = DefaultGenerator.class.getName().toLowerCase() + ".generator";

	private final static String GENERATOR_DEF = PropertiesUtils.get(DefaultGenerator.GENERATOR_KEY, IncrGenerator.NAME);

	private final Map<String, IDGenerator> ids = new HashMap<String, IDGenerator>();

	private final Profile profile;

	public DefaultGenerator(Profile profile, List<IDGenerator> ids) {
		this.profile = profile;
		for (IDGenerator each : ids) {
			this.ids.put(each.name(), each);
		}
	}

	@Override
	public Integer generate(Service service, Method method) {
		return this.generate(service, method.getName());
	}

	@Override
	public Integer generate(Service service, String method) {
		return this.ids.get(PropertiesUtils.profile(this.profile.profile(service), DefaultGenerator.GENERATOR_KEY, DefaultGenerator.GENERATOR_DEF)).generate(service, method);
	}

	public String name() {
		return DefaultGenerator.NAME;
	}
}
