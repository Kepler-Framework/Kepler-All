package com.kepler.zookeeper;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.annotation.Async;
import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;

/**
 * @author KimShen
 *
 */
@Internal
public class ZkFactoryConfig {

	public static final String RESET_KEY = "com.kepler.zookeeper.zkfactoryconfig.reset";

	private static final Log LOGGER = LogFactory.getLog(ZkFactoryConfig.class);

	private final AtomicInteger version = new AtomicInteger();

	private final ZkFactory factory;

	public ZkFactoryConfig(ZkFactory factory) {
		super();
		this.factory = factory;
	}

	/**
	 * 仅当指定版本等于内置版本时触发重置
	 * 
	 * @param version
	 */
	@Config(value = ZkFactoryConfig.RESET_KEY)
	@Async
	public void reset(int version) {
		// 如果请求版本小于当前版本则不进行更新
		if (this.version.get() == version) {
			this.version.getAndIncrement();
			this.factory.reset();
		} else {
			ZkFactoryConfig.LOGGER.warn("Unvalid version [current=" + this.version.get() + "][request=" + version + "]");
		}
	}
}
