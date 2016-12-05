package com.kepler.zookeeper;

import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;

/**
 * @author KimShen
 *
 */
@Internal
public class ZkFactoryConfig {

	public static final String RESET_KEY = "com.kepler.zookeeper.zkfactoryconfig.reset";

	private final ZkFactory factory;

	public ZkFactoryConfig(ZkFactory factory) {
		super();
		this.factory = factory;
	}

	/**
	 * 最大重试次数内重试
	 */
	@Config(value = ZkFactoryConfig.RESET_KEY)
	public void reset(String operator) {
		this.factory.reset(operator);
	}
}
