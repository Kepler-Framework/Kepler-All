package com.kepler.management.status.impl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import com.kepler.config.PropertiesUtils;
import com.kepler.management.status.Status;

/**
 * @author kim 2016年1月2日
 */
public class Status4Kepler implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	public void init() {
		this.status.put("kepler_path", new File("").getAbsolutePath());
		this.status.put("kepler_path_config", PropertiesUtils.FILE_CONFIG);
		this.status.put("kepler_path_dynamic", PropertiesUtils.FILE_DYNAMIC);
		this.status.put("kepler_path_version", PropertiesUtils.FILE_VERSION);
		this.status.put("kepler_version", PropertiesUtils.get("kepler.version"));
		this.status.put("kepler_arguments", ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
	}

	@Override
	public Map<String, Object> get() {
		return this.status;
	}
}
