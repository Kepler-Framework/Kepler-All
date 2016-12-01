package com.kepler.serial.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepler.protocol.Response;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;
import com.kepler.serial.jackson.JacksonSerial;

/**
 * 更快压缩速度
 * 
 * @author kim
 *
 *         2016年2月14日
 */
public class GenericSerial extends JacksonSerial implements SerialInput, SerialOutput {

	private static final String NAME = "generic";

	public static final byte SERIAL = 10;

	public GenericSerial() {
		super();
	}
	
	@Override
	protected ObjectMapper prepareRequestMapper() {
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return om;
	}
	
	@Override
	protected ObjectMapper prepareResponseMapper() {
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.addMixInAnnotations(Response.class, Mixin.class);
		return om;
	}
	

	abstract class Mixin {
		@JsonProperty abstract byte[] ack();
		@JsonProperty @JsonTypeInfo(use = Id.NONE) abstract Object response();
		@JsonProperty @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT) abstract Throwable throwable();
	}

	@Override
	public byte serial() {
		return GenericSerial.SERIAL;
	}

	@Override
	public String name() {
		return GenericSerial.NAME;
	}

}
