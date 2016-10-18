package com.kepler.connection.codec;

import java.util.HashMap;
import java.util.Map;

import com.kepler.config.PropertiesUtils;

/**
 * 黏包 (@see io.netty.handler.codec.LengthFieldBasedFrameDecoder)
 * 
 * @author kim 2015年7月8日
 */
public enum CodecHeader {

	ONE, TWO, FOUR, EIGHT;

	/**
	 * 黏包字节自身长度
	 */
	private static final CodecHeader CODEC = CodecHeader.valueOf(PropertiesUtils.get(CodecHeader.class.getName().toLowerCase() + ".codec", CodecHeader.FOUR.toString()));

	private static final Map<CodecHeader, Integer> MAPPING = new HashMap<CodecHeader, Integer>();

	static {
		MAPPING.put(ONE, 1);
		MAPPING.put(TWO, 2);
		MAPPING.put(FOUR, 4);
		MAPPING.put(EIGHT, 8);
	}

	public static final int DEFAULT = CodecHeader.CODEC.code();

	public int code() {
		return MAPPING.get(this).intValue();
	}
}
