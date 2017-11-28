package com.kepler.serial.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.kepler.KeplerSerialException;
import com.kepler.com.caucho.hessian.io.SerializerFactory;
import com.kepler.header.Headers;
import com.kepler.header.impl.LazyHeaders;
import com.kepler.org.apache.commons.io.IOUtils;
import com.kepler.protocol.Request;
import com.kepler.protocol.RequestFactories;
import com.kepler.protocol.Response;
import com.kepler.protocol.ResponseFactories;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;
import com.kepler.service.Service;

/**
 * @author kim 2016年2月2日
 */
abstract public class HessianSerial implements SerialOutput, SerialInput {

	private static final byte[] EMPTY = new byte[] {};

	private final SerializerFactory hessian2factory = new Hessian2SerializerFactory();

	private final HessianAdapter adapter = new HessianAdapter();

	/**
	 * 序列化分类
	 */
	private final Serializers serializers = new Serializers();

	private final ResponseFactories response;

	private final RequestFactories request;

	public HessianSerial(ResponseFactories response, RequestFactories request) {
		this.response = response;
		this.request = request;
	}

	abstract public byte serial();

	abstract public String name();

	abstract protected Integer buffer();

	abstract protected HessianInputProxy input(InputStream stream, Integer buffer);

	abstract protected HessianOutputProxy output(OutputStream stream, Integer buffer);

	public byte[] output(Object data, Class<?> clazz) throws KeplerSerialException {
		try (SegmentOutput output = new SegmentOutput(clazz)) {
			return output.writeObject(data).arrays();
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	public void output(Object data, Class<?> clazz, OutputStream stream, int buffer) throws KeplerSerialException {
		try (SegmentOutput output = new SegmentOutput(clazz, stream, buffer)) {
			output.writeObject(data);
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	public <T> T input(byte[] data, Class<T> clazz) throws KeplerSerialException {
		try (SegmentInput input = new SegmentInput(data, clazz)) {
			return this.adapter.adpater(clazz, input.readObject());
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	public <T> T input(InputStream input, int buffer, Class<T> clazz) throws KeplerSerialException {
		try (SegmentInput stream = new SegmentInput(input, buffer, clazz)) {
			return this.adapter.adpater(clazz, stream.readObject());
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	private class SegmentInput implements Closeable {

		private final HessianInputProxy input;

		private final Class<?> clazz;

		private SegmentInput(byte[] arrays, Class<?> clazz) {
			this.input = HessianSerial.this.input(new ByteArrayInputStream(arrays), HessianSerial.this.buffer()).setSerializerFactory(HessianSerial.this.hessian2factory);
			this.clazz = clazz;
		}

		private SegmentInput(InputStream stream, int buffer, Class<?> clazz) {
			this.input = HessianSerial.this.input(stream, buffer).setSerializerFactory(HessianSerial.this.hessian2factory);
			this.clazz = clazz;
		}

		@Override
		public void close() throws IOException {
			// Hessian级联关闭
			this.input.close();
		}

		public Object readObject() throws Exception {
			// 获取指定类型特定编码器
			return HessianSerial.this.serializers.get(this.clazz).read(this.input, this.clazz);
		}
	}

	private class SegmentOutput implements Closeable {

		private final Class<?> clazz;

		private final OutputStream stream;

		private final HessianOutputProxy output;

		private final ByteArrayOutputStream arrays;

		private SegmentOutput(Class<?> clazz) {
			this.output = HessianSerial.this.output(this.arrays = new ByteArrayOutputStream(HessianSerial.this.buffer()), HessianSerial.this.buffer()).setSerializerFactory(HessianSerial.this.hessian2factory);
			this.clazz = clazz;
			this.stream = null;
		}

		public SegmentOutput(Class<?> clazz, OutputStream stream, int buffer) {
			this.output = HessianSerial.this.output(this.stream = stream, buffer).setSerializerFactory(HessianSerial.this.hessian2factory);
			this.clazz = clazz;
			this.arrays = null;
		}

		/**
		 * 如果底层为字节数组则返回,否则返回空数组
		 * 
		 * @return
		 */
		public byte[] arrays() {
			return this.arrays != null ? this.arrays.toByteArray() : HessianSerial.EMPTY;
		}

		@Override
		public void close() throws IOException {
			// Output不会级联关闭
			this.output.close();
			IOUtils.closeQuietly(this.arrays);
			IOUtils.closeQuietly(this.stream);
		}

		public SegmentOutput writeObject(Object ob) throws Exception {
			// 获取指定类型特定解码器
			HessianSerial.this.serializers.get(this.clazz).write(this.output, ob);
			this.output.flush();
			return this;
		}
	}

	/**
	 * Request/Response降级适配器, Hessian序列化需要对某些属性特殊处理
	 * 
	 * @author kim 2016年1月11日
	 */
	private class HessianAdapter {

		public <T> T adpater(Class<T> clazz, Object instance) {
			return clazz.cast(Request.class.isAssignableFrom(instance.getClass()) ? new HessianRequest(Request.class.cast(instance)) : Response.class.isAssignableFrom(instance.getClass()) ? new HessianResponse(Response.class.cast(instance)) : instance);
		}
	}

	private class HessianRequest implements Request {

		private static final long serialVersionUID = 1L;

		private final Request actual;

		private HessianRequest(Request actual) {
			super();
			this.actual = actual;
		}

		/**
		 * 基础类型降级(Byte/Short/Float)
		 * 
		 * @param clazz
		 * @param instance
		 * @return
		 */
		private Object demotion(Class<?> clazz, Object instance) {
			return instance != null && (clazz.equals(Short.class) || clazz.equals(short.class)) ? Number.class.cast(instance).shortValue() : (clazz.equals(Float.class) || clazz.equals(float.class)) ? Number.class.cast(instance).floatValue() : (clazz.equals(Byte.class) || clazz.equals(byte.class)) ? Number.class.cast(instance).byteValue() : instance;
		}

		@Override
		public Service service() {
			return this.actual.service();
		}

		@Override
		public String method() {
			return this.actual.method();
		}

		public byte serial() {
			return this.actual.serial();
		}

		@Override
		public boolean async() {
			return this.actual.async();
		}

		public Class<?>[] types() {
			return this.actual.types();
		}

		@Override
		public Object[] args() {
			// 返回前降级
			for (int index = 0; index < this.actual.args().length; index++) {
				this.actual.args()[index] = this.demotion(this.types()[index], this.actual.args()[index]);
			}
			return this.actual.args();
		}

		@Override
		public byte[] ack() {
			return this.actual.ack();
		}

		@Override
		public Headers headers() {
			return this.actual.headers();
		}

		public String get(String key) {
			return this.actual.get(key);
		}

		public String get(String key, String def) {
			return this.actual.get(key, def);
		}

		public Request put(String key, String value) {
			return this.actual.put(key, value);
		}

		public Request putIfAbsent(String key, String value) {
			return this.actual.putIfAbsent(key, value);
		}

		public String toString() {
			return this.actual.toString();
		}
	}

	private class HessianResponse implements Response {

		private static final long serialVersionUID = 1L;

		private final Response actual;

		private HessianResponse(Response actual) {
			super();
			this.actual = actual;
		}

		private HessianResponse checkThrowable() {
			// Throwable是否反序列化失败(Hessian如果当前Classload不存在Exception类型将强转为Map造成后续调用异常栈静默)
			if (!this.actual.valid() && !Throwable.class.isAssignableFrom(this.actual.throwable().getClass())) {
				throw new KeplerSerialException(this.actual.throwable().toString());
			}
			return this;
		}

		@Override
		public byte[] ack() {
			return this.actual.ack();
		}

		@Override
		public byte serial() {
			return this.actual.serial();
		}

		@Override
		public Object response() {
			return this.actual.response();
		}

		@Override
		public Throwable throwable() {
			return this.actual.throwable();
		}

		@Override
		public boolean valid() {
			return this.checkThrowable().actual.valid();
		}

		@Override
		public HessianResponse resend(Throwable throwable) {
			this.actual.resend(throwable);
			return this;
		}

		public String toString() {
			return this.actual.toString();
		}
	}

	private class Serializers {

		private final Map<Class<?>, Serializer> serializers = new HashMap<Class<?>, Serializer>();

		/**
		 * 默认,如果无法获取指定序列化器则使用默认
		 */
		private final Serializer object = new ObjectSerializer();

		private Serializers() {
			this.serializers.put(Request.class, new RequestSerializer());
			this.serializers.put(Response.class, new ResponseSerializer());
		}

		/**
		 * 获取指定Class(精确匹配)对应编码/解码器,不存在则使用默认Object编码器
		 * 
		 * @param clazz
		 * @return
		 */
		public Serializer get(Class<?> clazz) {
			Serializer expect = this.serializers.get(clazz);
			return expect != null ? expect : this.object;
		}
	}

	private interface Serializer {

		public void write(HessianOutputProxy output, Object ob) throws Exception;

		public <T> T read(HessianInputProxy input, Class<T> clazz) throws Exception;
	}

	/**
	 * 完整Object序列化
	 * 
	 * @author kim 2016年2月1日
	 */
	private class ObjectSerializer implements Serializer {

		@Override
		public void write(HessianOutputProxy output, Object ob) throws Exception {
			output.writeObject(ob);
		}

		@Override
		public <T> T read(HessianInputProxy input, Class<T> clazz) throws Exception {
			return clazz.cast(input.readObject());
		}
	}

	/**
	 * Request序列化
	 * 
	 * @author kim 2016年2月1日
	 */
	private class RequestSerializer implements Serializer {

		private final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();

		/**
		 * Class.forname无法获取原生类型Class, 需要映射
		 */
		private RequestSerializer() {
			this.primitives.put(boolean.class.getName(), boolean.class);
			this.primitives.put(short.class.getName(), short.class);
			this.primitives.put(long.class.getName(), long.class);
			this.primitives.put(byte.class.getName(), byte.class);
			this.primitives.put(int.class.getName(), int.class);
		}

		/**
		 * Byte/Short/Float需要强制标注类型, Hessian无此类型
		 * 
		 * @param arg
		 * @return
		 */
		private boolean force(Class<?> clazz) {
			return byte.class.equals(clazz) || short.class.equals(clazz) || float.class.equals(clazz) || Byte.class.equals(clazz) || Short.class.equals(clazz) || Float.class.equals(clazz);
		}

		public void write(HessianOutputProxy output, Object object) throws Exception {
			this.write4request(output, Request.class.cast(object));
		}

		/**
		 * [Header|Args长度][Header实际数据][Args实际数据][元数据]
		 * 
		 * @param output
		 * @param request
		 * @throws Exception
		 */
		private void write4request(HessianOutputProxy output, Request request) throws Exception {
			// 原数据最后写提供预留空间
			this.write4length(output, request).write4header(output, request).write4args(output, request).write4metadata(output, request);
		}

		/**
		 * 高4位为Headers长度,低4位为Args长度
		 * 
		 * @param output
		 * @param request
		 * @return
		 * @throws Exception
		 */
		private RequestSerializer write4length(HessianOutputProxy output, Request request) throws Exception {
			int headers = request.headers() != null ? request.headers().length() : 0;
			output.writeInt(0 | headers << 0x4 | request.args().length);
			return this;
		}

		private RequestSerializer write4args(HessianOutputProxy output, Request request) throws Exception {
			for (int index = 0; index < request.types().length; index++) {
				// Type + Object(Hessian.writeObject)
				this.write4type(output, request, request.types()[index], request.args()[index]).writeObject(request.args()[index]);
			}
			return this;
		}

		private HessianOutputProxy write4type(HessianOutputProxy output, Request request, Class<?> clazz, Object arg) throws Exception {
			// this.force(clazz) 强制需要写入Type的类型
			// clazz.isPrimitive() 原生类型
			// arg !=null && arg.getClass().equals(clazz), 传递参数与声明类型完全一致
			if (!this.force(clazz) && (clazz.isPrimitive() || (arg != null && arg.getClass().equals(clazz)))) {
				output.writeBoolean(true);
			} else {
				output.writeBoolean(false);
				output.writeString(clazz.getName());
			}
			return output;
		}

		private RequestSerializer write4header(HessianOutputProxy output, Request request) throws Exception {
			if (request.headers() != null && request.headers().length() != 0) {
				for (String key : request.headers().keys()) {
					// String -> String
					output.writeString(key);
					output.writeString(request.get(key));
				}
			}
			return this;
		}

		private RequestSerializer write4metadata(HessianOutputProxy output, Request request) throws Exception {
			output.writeString(request.service().service());
			output.writeString(request.service().version());
			output.writeString(request.service().catalog());
			output.writeString(request.method());
			output.writeBytes(request.ack());
			return this;
		}

		public <T> T read(HessianInputProxy input, Class<T> clazz) throws Exception {
			return clazz.cast(this.read4request(input));
		}

		private Request read4request(HessianInputProxy input) throws Exception {
			// 计算长度
			Integer len = input.readInt();
			Integer len4args = len & 0xf;
			Integer len4headers = len >> 0x4;
			// 初始化Header/Args
			Headers headers = this.read4header(input, len4headers);
			Class<?>[] types = new Class[len4args];
			Object[] args = new Object[len4args];
			this.read4args(input, types, args, len4args);
			// 元数据
			Service service = new Service(input.readString(), input.readString(), input.readString());
			String method = input.readString();
			// ACK
			byte[] ack = input.readBytes();
			return HessianSerial.this.request.factory(HessianSerial.this.serial()).request(headers, service, method, false, args, types, ack, HessianSerial.this.serial());
		}

		private void read4args(HessianInputProxy input, Class<?>[] types, Object[] args, int length) throws Exception {
			for (int index = 0; index < length; index++) {
				// 是否Class/Object类型精确一致
				if (input.readBoolean()) {
					types[index] = (args[index] = input.readObject()).getClass();
				} else {
					// 首先查找基础类型
					String clazz = input.readString();
					types[index] = this.primitives.containsKey(clazz) ? this.primitives.get(clazz) : Class.forName(clazz);
					args[index] = input.readObject();
				}
			}
		}

		private Headers read4header(HessianInputProxy input, int length) throws Exception {
			Headers headers = null;
			if (length != 0) {
				headers = new LazyHeaders();
				for (int index = 0; index < length; index++) {
					headers.put(input.readString(), input.readString());
				}
			}
			return headers;
		}
	}

	/**
	 * Response序列化
	 * 
	 * @author kim 2016年2月1日
	 */
	private class ResponseSerializer implements Serializer {

		public void write(HessianOutputProxy output, Object object) throws Exception {
			this.write4response(output, Response.class.cast(object));
		}

		public <T> T read(HessianInputProxy input, Class<T> clazz) throws Exception {
			return clazz.cast(this.read4response(input));
		}

		private void write4response(HessianOutputProxy output, Response response) throws Exception {
			output.writeBytes(response.ack());
			output.writeBoolean(response.valid());
			if (response.valid()) {
				output.writeObject(response.response());
			} else {
				output.writeObject(response.throwable());
			}
		}

		private Response read4response(HessianInputProxy input) throws Exception {
			// 读取ACK
			byte[] ack = input.readBytes();
			if (input.readBoolean()) {
				// 正常返回
				return HessianSerial.this.response.factory(HessianSerial.this.serial()).response(ack, input.readObject(), HessianSerial.this.serial());
			} else {
				// 读取异常
				Object throwable = input.readObject();
				// 尝试解析异常, 如果为Throwable类型则直接抛出, 包装为类型错误
				Throwable actual = Throwable.class.isAssignableFrom(throwable.getClass()) ? Throwable.class.cast(throwable) : new ClassNotFoundException("Class not found when service throw exception, " + throwable.toString());
				return HessianSerial.this.response.factory(HessianSerial.this.serial()).throwable(ack, actual, HessianSerial.this.serial());
			}
		}
	}

	private class Hessian2SerializerFactory extends SerializerFactory {

		private Hessian2SerializerFactory() {
		}

		@Override
		public ClassLoader getClassLoader() {
			return Thread.currentThread().getContextClassLoader();
		}
	}
}
