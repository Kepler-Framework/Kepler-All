package com.kepler.serial.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerValidateException;
import com.kepler.config.PropertiesUtils;
import com.kepler.serial.SerialID;
import com.kepler.serial.SerialInput;
import com.kepler.serial.SerialOutput;
import com.kepler.serial.Serials;

/**
 * @author kim 2016年2月1日
 */
public class DefaultSerials implements Serials {

	private final static boolean SMART = PropertiesUtils.get(DefaultSerials.class.getName().toLowerCase() + ".smart", true);

	private final static Log LOGGER = LogFactory.getLog(DefaultSerials.class);

	private final DefaultSerialOutputs out;

	private final DefaultSerialInputs in;

	public DefaultSerials(DefaultSerialOutputs out, DefaultSerialInputs in) {
		super();
		this.out = out;
		this.in = in;
	}

	private SerialInput vaild(byte serial, SerialInput input) {
		if (input == null) {
			throw new KeplerValidateException("SerialInput: " + serial + " could not found ... ");
		}
		return input;
	}

	/**
	 * 如果开启Smart则使用默认Serial
	 * 
	 * @param name
	 * @param serial
	 * @return
	 */
	private byte smart(String name, SerialID serial) {
		KeplerValidateException exception = new KeplerValidateException("Serial: " + name + " could not found ... ");
		if (DefaultSerials.SMART) {
			DefaultSerials.LOGGER.warn(exception.getMessage());
			return serial.serial();
		}
		throw exception;
	}

	@Override
	public SerialInput input(byte serial) {
		return this.vaild(serial, this.in.inputs[serial]);
	}

	@Override
	public SerialOutput output(byte serial) {
		return this.out.outputs[serial];
	}

	public SerialInput def4input() {
		return this.in.def4input;
	}

	public SerialOutput def4output() {
		return this.out.def4output;
	}

	public byte input(String name) {
		SerialInput input = this.in.inputs4name.get(name);
		return input != null ? input.serial() : this.smart(name, this.in.def4input);
	}

	public byte output(String name) {
		SerialOutput output = this.out.outputs4name.get(name);
		return output != null ? output.serial() : this.smart(name, this.out.def4output);
	}
}
