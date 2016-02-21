package com.kepler.serial.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerValidateException;
import com.kepler.extension.Extension;
import com.kepler.serial.SerialInput;

/**
 * @author kim
 *
 * 2016年2月15日
 */
public class DefaultSerialInputs implements Extension {

	private final static Log LOGGER = LogFactory.getLog(DefaultSerialInputs.class);

	// Pakcage访问策略
	final Map<String, SerialInput> inputs4name = new HashMap<String, SerialInput>();

	// 0 - 127
	final SerialInput[] inputs = new SerialInput[Byte.MAX_VALUE];

	final SerialInput def4input;

	// 被DefaultSerialInputs引用的Input将不会被Extension加载(@see com.kepler.thread.ThreadFactoryConfig)
	public DefaultSerialInputs(SerialInput def4input) {
		super();
		this.install(def4input);
		this.def4input = def4input;
	}

	private DefaultSerialInputs warning(SerialInput input) {
		if (input != null) {
			throw new KeplerValidateException("Duplicate input: " + input.name() + " (" + input.serial() + ")");
		}
		return this;
	}

	@Override
	public DefaultSerialInputs install(Object instance) {
		SerialInput input = SerialInput.class.cast(instance);
		this.warning(this.inputs[input.serial()]);
		this.inputs[input.serial()] = input;
		this.inputs4name.put(input.name(), input);
		DefaultSerialInputs.LOGGER.info("Serial Input: " + input.name() + " completed ... ");
		return this;
	}

	@Override
	public Class<?> interested() {
		return SerialInput.class;
	}
}
