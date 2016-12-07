package com.kepler.serial.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepler.protocol.Response;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;
import com.kepler.serial.jackson.JacksonSerial;

/**
 * @author zhangjiehao
 *
 */
public class GenericSerial extends JacksonSerial implements SerialInput, SerialOutput {

	private static final String NAME = "generic";

	public static final byte SERIAL = 10;

	protected ObjectMapper prepare(ObjectMapper mapper) {
		ObjectMapper mapper_super = super.prepare(mapper);
		mapper_super.addMixInAnnotations(Response.class, Mixin.class);
		return mapper_super;
	}

	@Override
	public byte serial() {
		return GenericSerial.SERIAL;
	}

	@Override
	public String name() {
		return GenericSerial.NAME;
	}

	abstract class Mixin {

		@JsonProperty
		abstract byte[] ack();

		@JsonProperty
		@JsonTypeInfo(use = Id.NONE)
		abstract Object response();

		@JsonProperty
		@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
		abstract Throwable throwable();
	}
}
