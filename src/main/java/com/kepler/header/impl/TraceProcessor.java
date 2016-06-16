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
 * @author zhangjiehao 2016年1月6日
 */
public class TraceProcessor implements HeadersProcessor {

	/**
	 * 用以Hex
	 */
	private static final String ARRAY = PropertiesUtils.get(TraceProcessor.class.getName().toLowerCase() + ".array", "0123456789ABCDEF");

	private static final int SORT = PropertiesUtils.get(TraceProcessor.class.getName().toLowerCase() + ".sort", Integer.MAX_VALUE);

	private static final char[] HEX = TraceProcessor.ARRAY.toCharArray();

	private final GuidGenerator generator;

	private final Profile profile;

	public TraceProcessor(Profile profile, GuidGenerator idGenerator) {
		super();
		this.profile = profile;
		this.generator = idGenerator;
	}

	@Override
	public Headers process(Service service, Headers headers) {
		// 如果开启Trace则生成
		if (PropertiesUtils.profile(this.profile.profile(service), Trace.ENABLED_KEY, Trace.ENABLED_DEF) && headers != null) {
			// 如果已存在Trace ID则覆盖否则创建新Trace ID
			if (!StringUtils.isEmpty(headers.get(Trace.TRACE_COVER))) {
				headers.put(Trace.TRACE, headers.get(Trace.TRACE_COVER));
			} else {
				headers.put(Trace.TRACE, this.bytesToString(this.generator.generate()));
			}
			// 创建SPAN ID
			headers.put(Trace.SPAN, this.bytesToString(this.generator.generate()));
			// 创建Trace时间
			headers.put(Trace.START_TIME, String.valueOf(System.currentTimeMillis()));
		} else {
			if (headers != null) {
				resetTraceHeader(headers);
			}
		}
		return headers;
	}

	private void resetTraceHeader(Headers headers) {
		headers.put(Trace.TRACE, null);
		headers.put(Trace.SPAN, null);
		headers.put(Trace.SPAN_PARENT, null);
		headers.put(Trace.START_TIME, null);
		headers.put(Trace.TRACE_COVER, null);
	}

	private String bytesToString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = TraceProcessor.HEX[v >>> 4];
			hexChars[j * 2 + 1] = TraceProcessor.HEX[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public int sort() {
		return TraceProcessor.SORT;
	}
}
