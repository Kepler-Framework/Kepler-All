package com.kepler.connection.codec;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.serial.SerialID;
import com.kepler.serial.Serials;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.Service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.RecvByteBufAllocator.Handle;
import io.netty.util.ReferenceCountUtil;

/**
 * @author KimShen
 *
 */
public class Encoder implements Imported, Exported {

	/**
	 * 是否预估分配大小
	 */
	private static final boolean ESTIMATE = PropertiesUtils.get(Encoder.class.getName().toLowerCase() + ".estimate", true);

	/**
	 * 是否使用内存池分配
	 */
	private static final boolean POOLED = PropertiesUtils.get(Encoder.class.getName().toLowerCase() + ".pooled", true);

	/**
	 * 调整因子
	 */
	private static final double ADJUST = PropertiesUtils.get(Encoder.class.getName().toLowerCase() + ".adjust", 0.75);

	private static final Log LOGGER = LogFactory.getLog(Encoder.class);

	/**
	 * 内存分配
	 */
	private final ByteBufAllocator allocator = Encoder.POOLED ? PooledByteBufAllocator.DEFAULT : UnpooledByteBufAllocator.DEFAULT;

	private final Serials serials;

	private final Class<?> clazz;

	/**
	 * 缓存大小分配
	 */
	volatile private Map<ServiceAndMethod, Handle> estimates;

	public Encoder(Serials serials, Class<?> clazz) {
		super();
		this.clazz = clazz;
		this.serials = serials;
		this.estimates = new HashMap<ServiceAndMethod, Handle>();
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		this.install(service);
	}

	@Override
	public void subscribe(Service service) throws Exception {
		this.install(service);
	}

	/**
	 * 加载服务-方法级别的分配器(不考虑方法重载)
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void install(Service service) throws Exception {
		try {
			for (Method method : Service.clazz(service).getMethods()) {
				// 安全模式加载
				this.install(new ServiceAndMethod(service, method.getName()), AdaptiveRecvByteBufAllocator.DEFAULT.newHandle(), true);
			}
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			Encoder.LOGGER.info("Class not found: " + service);
		}
	}

	/**
	 * @param service_method
	 * @param handle
	 * @param secure 如果不为安全模式则采用COPY ON WRITE
	 * @return
	 */
	private Handle install(ServiceAndMethod service_method, Handle handle, boolean secure) {
		// 安全模式
		if (secure) {
			// 普通PUT
			this.estimates.put(service_method, handle);
		} else {
			synchronized (this) {
				// Double Check
				if (!this.estimates.containsKey(service_method)) {
					// Copy On Write
					Map<ServiceAndMethod, Handle> estimates = new HashMap<ServiceAndMethod, Handle>();
					for (ServiceAndMethod each : this.estimates.keySet()) {
						estimates.put(each, this.estimates.get(each));
					}
					estimates.put(service_method, handle);
					this.estimates = estimates;
					Encoder.LOGGER.info("Reset handles for: " + service_method);
				}
			}
		}
		return this.estimates.get(service_method);
	}

	/**
	 * 获取指定服务方法分配器
	 * 
	 * @param service_method
	 * @return
	 */
	private Handle handler(ServiceAndMethod service_method) {
		Handle handler = this.estimates.get(service_method);
		return handler != null ? handler : this.install(service_method, AdaptiveRecvByteBufAllocator.DEFAULT.newHandle(), false);
	}

	public ByteBuf encode(Service service, String method, Object message) throws Exception {
		ServiceAndMethod service_method = new ServiceAndMethod(service, method);
		// 分配ByteBuf
		ByteBuf buffer = Encoder.ESTIMATE ? this.handler(service_method).allocate(this.allocator) : this.allocator.ioBuffer();
		try {
			// 获取序列化策略(如Request/Response)
			byte serial = SerialID.class.cast(message).serial();
			// 首字节为序列化策略
			return WrapStream.class.cast(this.serials.output(serial).output(message, this.clazz, new WrapStream(service_method, buffer.writeByte(serial)), (int) (buffer.capacity() * Encoder.ADJUST))).record().buffer();
		} catch (Exception exception) {
			// 异常, 释放ByteBuf
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
			throw exception;
		}
	}

	private class WrapStream extends OutputStream {

		private final ServiceAndMethod service;

		private final ByteBuf buffer;

		private WrapStream(ServiceAndMethod service, ByteBuf buffer) {
			this.service = service;
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

		/**
		 * 写入完毕, 回调写入信息
		 * 
		 * @param estimate
		 * @return
		 */
		public WrapStream record() {
			// 预估大小更新(如果开启了预估)
			if (Encoder.ESTIMATE) {
				int readable = this.buffer.readableBytes();
				Encoder.this.estimates.get(this.service).record(readable);
			}
			return this;
		}
	}

	private class ServiceAndMethod {

		private final Service service;

		private final String method;

		private ServiceAndMethod(Service service, String method) {
			super();
			this.service = service;
			this.method = method;
		}

		public int hashCode() {
			return this.service.hashCode() ^ this.method.hashCode();
		}

		public boolean equals(Object ob) {
			// Guard case1, null
			if (ob == null) {
				return false;
			}
			ServiceAndMethod target = ServiceAndMethod.class.cast(ob);
			// Guard case2, 服务或方法不一致
			if (!this.service.equals(target.service) || !this.method.equals(target.method)) {
				return false;
			}
			return true;
		}

		public String toString() {
			return "[service=" + this.service + "][method=" + this.method + "]";
		}
	}
}
