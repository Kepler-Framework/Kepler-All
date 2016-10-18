package com.kepler.serial.jackson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.io.IOUtils;
import com.kepler.protocol.Request;
import com.kepler.protocol.Response;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;

/**
 * 更快压缩速度
 * 
 * @author kim
 *
 * 2016年2月14日
 */
public class JacksonSerial implements SerialInput, SerialOutput {

	/**
	 * 缓冲大小
	 */
	private static final int BUFFER = PropertiesUtils.get(JacksonSerial.class.getName().toLowerCase() + ".buffer", 0x4 << 6);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final ObjectReader READER_REQUEST = JacksonSerial.MAPPER.reader(Request.class);

	private static final ObjectReader READER_RESPONSE = JacksonSerial.MAPPER.reader(Response.class);

	private static final ObjectWriter WRITER_REQUEST = JacksonSerial.MAPPER.writerWithType(Request.class);

	private static final ObjectWriter WRITER_RESPONSE = JacksonSerial.MAPPER.writerWithType(Response.class);

	private final Serializers serializers = new Serializers();

	private static final byte[] EMPTY = new byte[] {};

	private static final String NAME = "jackson";

	private static final byte SERIAL = 1;

	public JacksonSerial() {
		JacksonSerial.MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public byte serial() {
		return JacksonSerial.SERIAL;
	}

	@Override
	public String name() {
		return JacksonSerial.NAME;
	}

	@Override
	public byte[] output(Object data, Class<?> clazz) throws Exception {
		try (AutoCloseOutput output = new AutoCloseOutput(clazz)) {
			return output.writeObject(data).arrays();
		}
	}

	@Override
	public OutputStream output(Object data, Class<?> clazz, OutputStream stream, int buffer) throws Exception {
		try (AutoCloseOutput output = new AutoCloseOutput(stream, buffer, clazz)) {
			output.writeObject(data);
		}
		return stream;
	}

	@Override
	public <T> T input(byte[] data, Class<T> clazz) throws Exception {
		try (AutoCloseInput input = new AutoCloseInput(data, clazz)) {
			return clazz.cast(input.readObject());
		}
	}

	@Override
	public <T> T input(InputStream input, int buffer, Class<T> clazz) throws Exception {
		try (AutoCloseInput stream = new AutoCloseInput(input, buffer, clazz)) {
			return clazz.cast(stream.readObject());
		}
	}

	private class AutoCloseInput implements Closeable {

		private final InputStream stream;

		private final Class<?> clazz;

		private AutoCloseInput(byte[] arrays, Class<?> clazz) {
			this.stream = new BufferedInputStream(new ByteArrayInputStream(arrays), JacksonSerial.BUFFER);
			this.clazz = clazz;
		}

		private AutoCloseInput(InputStream stream, int buffer, Class<?> clazz) {
			this.stream = new BufferedInputStream(stream, buffer);
			this.clazz = clazz;
		}

		@Override
		public void close() throws IOException {
			IOUtils.closeQuietly(this.stream);
		}

		public Object readObject() throws Exception {
			return JacksonSerial.this.serializers.get(this.clazz).read(this.stream, this.clazz);
		}
	}

	private class AutoCloseOutput implements Closeable {

		private final ByteArrayOutputStream arrays;

		private final OutputStream stream;

		private final Class<?> clazz;

		private AutoCloseOutput(Class<?> clazz) {
			this.stream = new BufferedOutputStream(this.arrays = new ByteArrayOutputStream(JacksonSerial.BUFFER), JacksonSerial.BUFFER);
			this.clazz = clazz;
		}

		private AutoCloseOutput(OutputStream stream, int buffer, Class<?> clazz) {
			this.stream = new BufferedOutputStream(stream, buffer);
			this.clazz = clazz;
			this.arrays = null;
		}

		/**
		 * 如果底层为字节数组则返回,否则返回空数组
		 * 
		 * @return
		 */
		public byte[] arrays() {
			return this.arrays != null ? this.arrays.toByteArray() : JacksonSerial.EMPTY;
		}

		@Override
		public void close() throws IOException {
			// Output不会级联关闭
			IOUtils.closeQuietly(this.arrays);
			IOUtils.closeQuietly(this.stream);
		}

		public AutoCloseOutput writeObject(Object ob) throws Exception {
			JacksonSerial.this.serializers.get(this.clazz).write(this.stream, ob);
			return this;
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

		public void write(OutputStream output, Object ob) throws Exception;

		public <T> T read(InputStream input, Class<T> clazz) throws Exception;
	}

	private class ObjectSerializer implements Serializer {

		@Override
		public void write(OutputStream output, Object ob) throws Exception {
			JacksonSerial.MAPPER.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.MAPPER.readValue(input, clazz);
		}
	}

	private class RequestSerializer implements Serializer {

		@Override
		public void write(OutputStream output, Object ob) throws Exception {
			JacksonSerial.WRITER_REQUEST.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.READER_REQUEST.readValue(input);
		}
	}

	private class ResponseSerializer implements Serializer {

		@Override
		public void write(OutputStream output, Object ob) throws Exception {
			JacksonSerial.WRITER_RESPONSE.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.READER_RESPONSE.readValue(input);
		}
	}
}
