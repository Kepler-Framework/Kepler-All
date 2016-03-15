package com.kepler.header.impl;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.impl.GuidGenerator;
import com.kepler.service.Service;
import com.kepler.trace.Trace;

/**
 * 在Header中保存当前线程Trace跟踪号
 * 
 * @author kim 2016年1月6日
 */
public class TraceProcessor implements HeadersProcessor {

	private static final int SORT = PropertiesUtils.get(TraceProcessor.class.getName().toLowerCase() + ".sort", Integer.MAX_VALUE);

	private final Profile profile;

	private final GuidGenerator generator;

	public TraceProcessor(Profile profile, GuidGenerator idGenerator) {
		super();
		this.profile = profile;
		this.generator = idGenerator;
	}

	@Override
	public Headers process(Service service, Headers headers) {
		// 如果开启Trace则生成
		if (PropertiesUtils.profile(this.profile.profile(service), Trace.ENABLED_KEY, Trace.ENABLED_DEF)) {
			headers.put(Trace.SPAN, this.generator.generate().toString());
			headers.put(Trace.START_TIME, String.valueOf(System.currentTimeMillis()));
		}
		return headers;
	}

	@Override
	public int sort() {
		return TraceProcessor.SORT;
	}
}
