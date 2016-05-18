package com.kepler.header.impl;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.impl.GuidGenerator;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.service.Service;
import com.kepler.trace.Trace;

/**
 * 在Header中保存当前线程Trace跟踪号
 * 
 * @author kim 2016年1月6日
 */
public class TraceProcessor implements HeadersProcessor {

	private static final int SORT = PropertiesUtils.get(TraceProcessor.class.getName().toLowerCase() + ".sort", Integer.MAX_VALUE);

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	
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
		if (PropertiesUtils.profile(this.profile.profile(service), Trace.ENABLED_KEY, Trace.ENABLED_DEF) && headers != null) {
			if (!StringUtils.isEmpty(headers.get(Trace.TRACE_TO_COVER))) {
				headers.put(Trace.TRACE, headers.get(Trace.TRACE_TO_COVER));
			} else {
				headers.put(Trace.TRACE, bytesToString(this.generator.generate()));
			}
			headers.put(Trace.SPAN, bytesToString(this.generator.generate()));
			headers.put(Trace.START_TIME, String.valueOf(System.currentTimeMillis()));
		}
		return headers;
	}

	private String bytesToString(byte[] bytes) {
	   char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	@Override
	public int sort() {
		return TraceProcessor.SORT;
	}
}
