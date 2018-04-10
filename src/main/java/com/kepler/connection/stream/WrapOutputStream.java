package com.kepler.connection.stream;

import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.RecvByteBufAllocator.Handle;

/**
 * @author KimShen
 *
 */
public class WrapOutputStream extends OutputStream {

	private final ByteBuf buffer;

	public WrapOutputStream(ByteBuf buffer) {
		this.buffer = buffer;
	}

	/**
	 * 安全性校验
	 * 
	 * @param dest
	 * @param offset
	 * @param length
	 */
	private void valid(byte[] dest, int offset, int length) {
		if (dest == null) {
			throw new NullPointerException();
		} else if ((offset < 0) || (offset > dest.length) || (length < 0) || ((offset + length) > dest.length) || ((offset + length) < 0)) {
			throw new IndexOutOfBoundsException();
		}
	}

	public ByteBuf buffer() {
		return this.buffer;
	}

	@Override
	public void write(int data) {
		this.buffer.writeByte(data);
	}

	public void write(byte[] src) {
		this.write(src, 0, src.length);
	}

	public void write(byte[] src, int offset, int length) {
		this.valid(src, offset, length);
		if (length == 0) {
			return;
		}
		this.buffer.writeBytes(src, offset, length);
	}

	public WrapOutputStream reset() {
		this.buffer.writerIndex(1);
		return this;
	}

	/**
	 * 写入完毕, 回调写入信息
	 * 
	 * @param estimate
	 * @return
	 */
	public WrapOutputStream record(Handle handle) {
		handle.record(this.buffer.readableBytes());
		return this;
	}
}
