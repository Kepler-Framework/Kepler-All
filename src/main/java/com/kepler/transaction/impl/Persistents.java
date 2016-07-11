package com.kepler.transaction.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerPersistentException;
import com.kepler.config.PropertiesUtils;
import com.kepler.extension.Extension;
import com.kepler.transaction.Persistent;
import com.kepler.transaction.Request;

/**
 * @author KimShen
 *
 */
public class Persistents implements Persistent, Extension {

	/**
	 * 持久化策略, 默认文件持久化
	 */
	private static final String NAME = PropertiesUtils.get(Persistents.class.getName().toLowerCase() + ".name", FilePersistent.NAME);

	private static final Log LOGGER = LogFactory.getLog(Persistents.class);

	private final Map<String, Persistent> persistents = new HashMap<String, Persistent>();

	@Override
	public Persistents install(Object instance) {
		Persistent persistent = Persistent.class.cast(instance);
		this.persistents.put(persistent.name(), persistent);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Persistent.class;
	}

	public String name() {
		return "mixed";
	}

	@Override
	public List<Request> list() {
		Persistents.LOGGER.info("Persistents: " + this.persistents);
		Persistent persistent = this.persistents.get(Persistents.NAME);
		return persistent.list();
	}

	@Override
	public void release(String uuid) throws KeplerPersistentException {
		this.persistents.get(Persistents.NAME).release(uuid);
	}

	@Override
	public void persist(Request request) throws KeplerPersistentException {
		this.persistents.get(Persistents.NAME).persist(request);
	}
}
