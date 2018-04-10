package com.kepler.connection.codec;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerSerialException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.stream.WrapOutputStream;
import com.kepler.protocol.Protocols;
import com.kepler.serial.SerialID;
import com.kepler.serial.SerialOutput;
import com.kepler.serial.SerialResend;
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

	/**
	 * 缓存大小分配
	 */
	volatile private Map<ServiceAndMethod, Handle> estimates = new HashMap<ServiceAndMethod, Handle>();

	private final Protocols protocols;

	private final Serials serials;

	public Encoder(Serials serials, Protocols protocols) {
		super();
		this.protocols = protocols;
		this.serials = serials;
	}

	private Handle install(ServiceAndMethod service_method) throws Exception {
		synchronized (this) {
			Handle handle = this.estimates.get(service_method);
			// Double Check
			if (handle != null) {
				return handle;
			}
			Map<ServiceAndMethod, Handle> estimates = new HashMap<ServiceAndMethod, Handle>(this.estimates);
			estimates.put(service_method, (handle = AdaptiveRecvByteBufAllocator.DEFAULT.newHandle()));
			this.estimates = estimates;
			return handle;
		}
	}

	/**
	 * 加载服务-方法级别的分配器(不考虑方法重载)
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void install(Service service) throws Exception {
		try {
			Map<ServiceAndMethod, Handle> estimates = new HashMap<ServiceAndMethod, Handle>(this.estimates);
			for (Method method : Service.clazz(service).getMethods()) {
				ServiceAndMethod service_method = new ServiceAndMethod(service, method.getName());
				estimates.put(service_method, AdaptiveRecvByteBufAllocator.DEFAULT.newHandle());
			}
			this.estimates = estimates;
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			Encoder.LOGGER.info("Class not found: " + service);
		}
	}

	private void uninstall(Service service) throws Exception {
		Map<ServiceAndMethod, Handle> estimates = new HashMap<ServiceAndMethod, Handle>(this.estimates);
		estimates.remove(service);
		this.estimates = estimates;
	}

	@Override
	public void export(Service service, Object instance) throws Exception {
		this.install(service);
	}

	public void logout(Service service) throws Exception {
		this.uninstall(service);
	}

	@Override
	public void subscribe(Service service) throws Exception {
		this.install(service);
	}

	public void unsubscribe(Service service) throws Exception {
		this.uninstall(service);
	}

	/**
	 * 分配预估缓存
	 * 
	 * @param service_method
	 * @return
	 * @throws Exception
	 */
	private ByteBuf estimate(ServiceAndMethod service_method) throws Exception {
		if (Encoder.ESTIMATE) {
			Handle handle = this.estimates.get(service_method);
			return handle != null ? handle.allocate(this.allocator) : this.install(service_method).allocate(this.allocator);
		} else {
			return this.allocator.ioBuffer();
		}
	}

	/**
	 * @param output  序列化工厂
	 * @param stream 
	 * @param buffer  缓存大小
	 * @param message 实际报文
	 * @param clazz 序列化类型
	 * @return
	 */
	private WrapOutputStream stream(SerialOutput output, WrapOutputStream stream, Integer buffer, Class<?> clazz, Object message) throws KeplerSerialException {
		try {
			output.output(message, clazz, stream, buffer);
		} catch (KeplerSerialException e) {
			// 如果为序列化错误检查是否需要重发
			if (SerialResend.class.isAssignableFrom(message.getClass())) {
				Encoder.LOGGER.error(e.getMessage(), e);
				output.output(SerialResend.class.cast(message).resend(e), clazz, stream.reset(), buffer);
			} else {
				throw e;
			}
		}
		return stream;
	}

	public ByteBuf encode(Service service, String method, Object message) throws Exception {
		// 序列化策略
		byte serial_id = SerialID.class.cast(message).serial();
		// 序列化实现类
		SerialOutput serial_output = this.serials.output(serial_id);
		ServiceAndMethod service_method = new ServiceAndMethod(service, method);
		// 分配缓存
		ByteBuf buffer = this.estimate(service_method);
		try (WrapOutputStream stream = new WrapOutputStream(buffer.writeByte(serial_id))) {
			WrapOutputStream output = this.stream(serial_output, stream, (int) (buffer.capacity() * Encoder.ADJUST), this.protocols.protocol(serial_id), message);
			if (Encoder.ESTIMATE) {
				output.record(this.estimates.get(service_method)).buffer();
			}
			return output.buffer();
		} catch (Exception exception) {
			// 异常, 释放ByteBuf
			if (buffer.refCnt() > 0) {
				ReferenceCountUtil.release(buffer);
			}
			throw exception;
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
