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
			this.process4trace(headers);
			this.process4span(headers);
		} else {
			if (headers != null) {
				this.reset(headers);
			}
		}
		return headers;
	}

	private void process4span(Headers headers) {
		// 创建SPAN ID
		headers.putIfAbsent(Trace.SPAN, this.bytesToString(this.generator.generate()));
		// 创建Trace时间
		headers.putIfAbsent(Trace.START_TIME, String.valueOf(System.currentTimeMillis()));
	}

	private void process4trace(Headers headers) {
		// 如果已存在Trace ID则覆盖否则创建新Trace ID
		if (!StringUtils.isEmpty(headers.get(Trace.TRACE_COVER))) {
			headers.putIfAbsent(Trace.TRACE, headers.get(Trace.TRACE_COVER));
		} else {
			headers.putIfAbsent(Trace.TRACE, this.bytesToString(this.generator.generate()));
		}
	}

	private void reset(Headers headers) {
		headers.delete(Trace.TRACE);
		headers.delete(Trace.TRACE_COVER);
		headers.delete(Trace.SPAN);
		headers.delete(Trace.SPAN_PARENT);
		headers.delete(Trace.START_TIME);
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
