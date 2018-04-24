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
import com.kepler.KeplerSerialException;
import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.io.IOUtils;
import com.kepler.protocol.impl.JacksonRequest;
import com.kepler.protocol.impl.JacksonResponse;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;

/**
 * @author kim
 *
 * 2016年2月14日
 */
public class JacksonSerial implements SerialInput, SerialOutput {

	private static final boolean ACTIVED = PropertiesUtils.get(JacksonSerial.class.getName().toLowerCase() + ".actived", true);

	/**
	 * 缓冲大小
	 */
	private static final int BUFFER = PropertiesUtils.get(JacksonSerial.class.getName().toLowerCase() + ".buffer", 0x4 << 6);

	private final Serializers serializers = new Serializers();

	private static final byte[] EMPTY = new byte[] {};

	private static final String NAME = "jackson";

	public static final byte SERIAL = 2;

	private boolean actived = Boolean.TRUE;

	private ObjectReader reader_request;

	private ObjectReader reader_response;

	private ObjectWriter writer_request;

	private ObjectWriter writer_response;

	private ObjectMapper mapper;

	public JacksonSerial() {
		if (!JacksonSerial.ACTIVED) {
			this.actived = false;
			return;
		}
		try {
			Class.forName(ObjectMapper.class.getName());
			Class.forName(ObjectWriter.class.getName());
			Class.forName(ObjectReader.class.getName());
			this.mapper = this.prepare(new ObjectMapper());
			this.reader_request = this.mapper.reader(JacksonRequest.class);
			this.reader_response = this.mapper.reader(JacksonResponse.class);
			this.writer_request = this.mapper.writerWithType(JacksonRequest.class);
			this.writer_response = this.mapper.writerWithType(JacksonResponse.class);
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			this.actived = false;
		}
	}

	protected ObjectMapper prepare(ObjectMapper mapper) {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	@Override
	public String name() {
		return JacksonSerial.NAME;
	}

	@Override
	public byte serial() {
		return JacksonSerial.SERIAL;
	}

	public boolean actived() {
		return this.actived;
	}

	@Override
	public byte[] output(Object data, Class<?> clazz) throws KeplerSerialException {
		try (AutoCloseOutput output = new AutoCloseOutput(clazz)) {
			return output.writeObject(data).arrays();
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	@Override
	public void output(Object data, Class<?> clazz, OutputStream stream, int buffer) throws KeplerSerialException {
		try (AutoCloseOutput output = new AutoCloseOutput(stream, buffer, clazz)) {
			output.writeObject(data);
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	@Override
	public <T> T input(byte[] data, Class<T> clazz) throws KeplerSerialException {
		try (AutoCloseInput input = new AutoCloseInput(data, clazz)) {
			return clazz.cast(input.readObject());
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
		}
	}

	@Override
	public <T> T input(InputStream input, int buffer, Class<T> clazz) throws KeplerSerialException {
		try (AutoCloseInput stream = new AutoCloseInput(input, buffer, clazz)) {
			return clazz.cast(stream.readObject());
		} catch (Exception e) {
			throw new KeplerSerialException(e.getMessage());
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
			this.serializers.put(JacksonRequest.class, new RequestSerializer());
			this.serializers.put(JacksonResponse.class, new ResponseSerializer());
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
			JacksonSerial.this.mapper.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.this.mapper.readValue(input, clazz);
		}
	}

	private class RequestSerializer implements Serializer {

		@Override
		public void write(OutputStream output, Object ob) throws Exception {
			JacksonSerial.this.writer_request.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.this.reader_request.readValue(input);
		}
	}

	private class ResponseSerializer implements Serializer {

		@Override
		public void write(OutputStream output, Object ob) throws Exception {
			JacksonSerial.this.writer_response.writeValue(output, ob);
		}

		@Override
		public <T> T read(InputStream input, Class<T> clazz) throws Exception {
			return JacksonSerial.this.reader_response.readValue(input);
		}
	}
}
