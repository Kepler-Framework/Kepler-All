package com.kepler.serial.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerValidateException;
import com.kepler.extension.Extension;
import com.kepler.serial.SerialOutput;

/**
 * @author kim
 *
 * 2016年2月15日
 */
public class DefaultSerialOutputs implements Extension {

	private static final Log LOGGER = LogFactory.getLog(DefaultSerialOutputs.class);

	// Pakcage访问策略
	final Map<String, SerialOutput> outputs4name = new HashMap<String, SerialOutput>();

	// 0 - 127
	final SerialOutput[] outputs = new SerialOutput[Byte.MAX_VALUE];

	final SerialOutput def4output;

	// 被DefaultSerialOutputs引用的Output将不会被Extension加载(@see com.kepler.thread.ThreadFactoryConfig)
	public DefaultSerialOutputs(SerialOutput def4output) {
		super();
		this.install(def4output);
		this.def4output = def4output;
	}

	private DefaultSerialOutputs warning(SerialOutput output) {
		if (output != null) {
			throw new KeplerValidateException("Duplicate output: " + output.name() + " (" + output.serial() + ")");
		}
		return this;
	}

	@Override
	public DefaultSerialOutputs install(Object instance) {
		SerialOutput output = SerialOutput.class.cast(instance);
		this.warning(this.outputs[output.serial()]);
		this.outputs[output.serial()] = output;
		this.outputs4name.put(output.name(), output);
		DefaultSerialOutputs.LOGGER.info("Serial Output: " + output.name() + " completed ... ");
		return this;
	}

	@Override
	public Class<?> interested() {
		return SerialOutput.class;
	}
}
