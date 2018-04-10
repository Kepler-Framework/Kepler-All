package com.kepler.connection.stream;

import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;

/**
 * @author KimShen
 *
 */
public class WrapInputStream extends InputStream {

	/**
	 * 当前数据集
	 */
	private ByteBuf buffer;

	/**
	 * 当前可读数量
	 */
	private int readable;

	/**
	 * 当前读取索引
	 */
	private int position;

	public WrapInputStream(ByteBuf buffer) {
		this.readable = (this.buffer = buffer).readableBytes();
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
		} else if (offset < 0 || length < 0 || length > dest.length - offset) {
			throw new IndexOutOfBoundsException();
		}
	}

	public int read() {
		if (this.readable > this.position) {
			int read = this.buffer.readByte() & 0xff;
			this.position++;
			return read;
		} else {
			return -1;
		}
	}

	public int read(byte[] dest) {
		return this.read(dest, 0, dest.length);
	}

	public int read(byte[] dest, int offset, int length) {
		this.valid(dest, offset, length);
		// 是否可读
		if (this.position >= this.readable) {
			return -1;
		}
		// 剩余可读
		int available = this.readable - this.position;
		// 本次实际允许读取的长度
		int length4actual = length > available ? available : length;
		if (length4actual <= 0) {
			return -1;
		}
		this.buffer.readBytes(dest, 0, length4actual);
		this.position += length4actual;
		return length4actual;
	}

	public int available() throws IOException {
		return this.buffer.readableBytes();
	}
}