package com.kepler.connection.codec;

import java.io.OutputStream;

import com.kepler.config.PropertiesUtils;
import com.kepler.serial.SerialID;
import com.kepler.serial.Serials;
import com.kepler.traffic.Traffic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.RecvByteBufAllocator.Handle;
import io.netty.util.ReferenceCountUtil;

/**
 * @author KimShen
 *
 */
public class Encoder {

	/**
	 * 调整因子
	 */
	private static final double ADJUST = PropertiesUtils.get(Encoder.class.getName().toLowerCase() + ".adjust", 0.75);

	/**
	 * 可重用OUTPUT
	 */
	private static final ThreadLocal<BufferOutputStream> OUTPUT = new ThreadLocal<BufferOutputStream>() {
		protected BufferOutputStream initialValue() {
			return new BufferOutputStream();
		}
	};

	/**
	 * 分配计算
	 */
	private final Handle estimate = AdaptiveRecvByteBufAllocator.DEFAULT.newHandle();

	private final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

	private final Serials serials;

	private final Traffic traffic;

	private final Class<?> clazz;

	public Encoder(Traffic traffic, Serials serials, Class<?> clazz) {
		super();
		this.clazz = clazz;
		this.serials = serials;
		this.traffic = traffic;
	}

	public ByteBuf encode(Object message) throws Exception {
		// 分配ByteBuf(预测大小)
		ByteBuf buffer = this.estimate.allocate(this.allocator);
		try {
			// 获取序列化策略(如Request/Response)
			byte serial = SerialID.class.cast(message).serial();
			// 首字节为序列化策略
			return BufferOutputStream.class.cast(this.serials.output(serial).output(message, this.clazz, Encoder.OUTPUT.get().reset(buffer.writeByte(serial)), (int) (buffer.capacity() * Encoder.ADJUST))).record(this.traffic, this.estimate).buffer();
		} catch (Exception exception) {
			// 异常, 释放ByteBuf
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
			throw exception;
		}
	}

	private static class BufferOutputStream extends OutputStream {

		private ByteBuf buffer;

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

		public BufferOutputStream reset(ByteBuf buffer) {
			this.buffer = buffer;
			return this;
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

		/**
		 * 写入完毕, 回调写入信息
		 * 
		 * @param traffic
		 * @param estimate
		 * @return
		 */
		public BufferOutputStream record(Traffic traffic, Handle estimate) {
			int readable = this.buffer.readableBytes();
			// 流量记录
			traffic.output(readable);
			// 预估大小更新
			estimate.record(readable);
			return this;
		}
	}
}
