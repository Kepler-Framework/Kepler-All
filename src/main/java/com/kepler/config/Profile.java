package com.kepler.config;

import java.util.HashMap;
import java.util.Map;

import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.service.Service;

/**
 * 偏好
 * 
 * @author kim 2015年11月16日
 */
public class Profile {

	/**
	 * 是否开启偏好
	 */
	private static final boolean ENABLED = PropertiesUtils.get(Profile.class.getName().toLowerCase() + ".enabled", false);

	private final Map<Service, String> profiles = new HashMap<Service, String>();

	/**
	 * 如果没有指定偏好则使用全类名作为偏好
	 * 
	 * @param service
	 * @param profile
	 * @return
	 */
	public Profile add(Service service, String profile) {
		// 如果Profile为空(Null | "")则使用全类名
		this.profiles.put(service, StringUtils.isEmpty(profile) ? service.service().getName().toLowerCase() : profile);
		return this;
	}

	public String profile(Service service) {
		return Profile.ENABLED ? this.profiles.get(service) : null;
	}
}
