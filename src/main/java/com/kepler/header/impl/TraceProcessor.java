package com.kepler.header.impl;

import java.util.UUID;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.header.HeadersProcessor;
import com.kepler.service.Service;
import com.kepler.trace.Trace;

/**
 * 在Header中保存当前线程Trace跟踪号
 * 
 * @author kim 2016年1月6日
 */
public class TraceProcessor implements HeadersProcessor {

	private final static int SORT = PropertiesUtils.get(TraceProcessor.class.getName().toLowerCase() + ".sort", Integer.MAX_VALUE);

	private final Profile profile;

	public TraceProcessor(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public Headers process(Service service, Headers headers) {
		// 如果开启Trace则生成
		return PropertiesUtils.profile(this.profile.profile(service), Trace.ENABLED_KEY, Trace.ENABLED_DEF) ? headers.put(Trace.TRACE, UUID.randomUUID().toString()) : headers;
	}

	@Override
	public int sort() {
		return TraceProcessor.SORT;
	}
}
