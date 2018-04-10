package com.kepler.connection.codec;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.stream.WrapInputStream;
import com.kepler.protocol.Protocols;
import com.kepler.serial.Serials;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * @author KimShen
 *
 */
public class Decoder {

	/**
	 * 调整因子
	 */
	private static final double ADJUST = PropertiesUtils.get(Decoder.class.getName().toLowerCase() + ".adjust", 0.75);

	private final Protocols protocols;

	private final Serials serials;

	public Decoder(Serials serials, Protocols protocols) {
		super();
		this.protocols = protocols;
		this.serials = serials;
	}

	public Object decode(ByteBuf buffer) throws Exception {
		try {
			// buffer.readByte(), 首个字节保存序列化策略
			// buffer.readableBytes() * Decoder.ADJUST确定Buffer大小
			byte serial = buffer.readByte();
			return this.serials.input(serial).input(new WrapInputStream(buffer), (int) (buffer.readableBytes() * Decoder.ADJUST), this.protocols.protocol(serial));
		} finally {
			// 释放引用
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
		}
	}
}
