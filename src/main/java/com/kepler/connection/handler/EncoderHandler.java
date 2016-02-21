package com.kepler.connection.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.RecvByteBufAllocator.Handle;
import io.netty.util.ReferenceCountUtil;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.impl.ExceptionListener;
import com.kepler.serial.SerialID;
import com.kepler.serial.Serials;
import com.kepler.traffic.Traffic;

/**
 * @author kim 2015年7月8日
 */
@Sharable
public class EncoderHandler extends ChannelOutboundHandlerAdapter {

	/**
	 * 调整因子
	 */
	private final static double ADJUST = PropertiesUtils.get(EncoderHandler.class.getName().toLowerCase() + ".adjust", 0.75);

	private final static Log LOGGER = LogFactory.getLog(EncoderHandler.class);

	/**
	 * 可重用OUTPUT
	 */
	private final static ThreadLocal<BufferOutputStream> OUTPUT = new ThreadLocal<BufferOutputStream>() {
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

	public EncoderHandler(Traffic traffic, Serials serials, Class<?> clazz) {
		super();
		this.clazz = clazz;
		this.serials = serials;
		this.traffic = traffic;
	}

	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// 分配ByteBuf(预测大小)
		ByteBuf buffer = this.estimate.allocate(this.allocator);
		try {
			// 获取序列化策略(如Request/Response)
			byte serial = SerialID.class.cast(msg).serial();
			// 首字节为序列化策略
			ctx.writeAndFlush(BufferOutputStream.class.cast(this.serials.output(serial).output(msg, this.clazz, EncoderHandler.OUTPUT.get().reset(buffer.writeByte(serial)), (int) (buffer.capacity() * EncoderHandler.ADJUST))).record(this.traffic, this.estimate).buffer()).addListener(ExceptionListener.TRACE);
		} catch (Throwable throwable) {
			// 异常, 释放ByteBuf
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
			EncoderHandler.LOGGER.error(throwable.getMessage(), throwable);
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
