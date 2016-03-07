package com.kepler.header.impl;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.header.HeadersProcessor;
import com.kepler.host.Host;
import com.kepler.service.Service;

/**
 * 在Header中保存当前主机Tag
 * 
 * @author kim 2015年8月3日
 */
public class TagProcessor implements HeadersProcessor {

	private static final int SORT = PropertiesUtils.get(TagProcessor.class.getName().toLowerCase() + ".sort", Integer.MAX_VALUE);

	private final Profile profile;

	public TagProcessor(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public Headers process(Service service, Headers headers) {
		String tag = PropertiesUtils.profile(this.profile.profile(service), Host.TAG_KEY, Host.TAG_VAL);
		// 如果Profile Tag = 默认Tag则不标记
		return Host.TAG_DEF.equals(tag) ? headers : headers.putIfAbsent(Host.TAG_KEY, tag);
	}

	@Override
	public int sort() {
		return TagProcessor.SORT;
	}
}
