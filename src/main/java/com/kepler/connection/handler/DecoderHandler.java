package com.kepler.connection.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.serial.Serials;
import com.kepler.traffic.Traffic;

/**
 * @author kim 2015年7月8日
 */
@Sharable
public class DecoderHandler extends ChannelInboundHandlerAdapter {

	/**
	 * 调整因子
	 */
	private static final double ADJUST = PropertiesUtils.get(DecoderHandler.class.getName().toLowerCase() + ".adjust", 0.75);

	private static final Log LOGGER = LogFactory.getLog(DecoderHandler.class);

	/**
	 * 可复用Input
	 */
	private static final ThreadLocal<BufferInputStream> INPUT = new ThreadLocal<BufferInputStream>() {
		protected BufferInputStream initialValue() {
			return new BufferInputStream();
		}
	};

	private final Serials serials;

	private final Traffic traffic;

	private final Class<?> clazz;

	public DecoderHandler(Traffic traffic, Serials serials, Class<?> clazz) {
		super();
		this.clazz = clazz;
		this.traffic = traffic;
		this.serials = serials;
	}

	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
		ByteBuf buffer = ByteBuf.class.cast(message);
		try {
			// 流量统计(Input)
			this.traffic.input(buffer.readableBytes());
			// buffer.readByte(), 首个字节保存序列化策略
			// buffer.readableBytes() * DecoderHandler.ADJUST确定Buffer大小
			ctx.fireChannelRead(
					this.serials.input(buffer.readByte())
						.input(
								DecoderHandler.INPUT.get().reset(buffer), (int) (buffer.readableBytes() * DecoderHandler.ADJUST), this.clazz
								)
					);
		} catch (Throwable throwable) {
			DecoderHandler.LOGGER.error("From:(" + ctx.channel().remoteAddress() + ") " + throwable.getMessage(), throwable);
		} finally {
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
		}
	}

	private static class BufferInputStream extends InputStream {

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

		// 重置
		public BufferInputStream reset(ByteBuf buffer) {
			this.readable = (this.buffer = buffer).readableBytes();
			this.position = 0;
			return this;
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
	}
}
