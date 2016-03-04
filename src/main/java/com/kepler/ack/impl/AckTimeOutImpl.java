package com.kepler.ack.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Ack;
import com.kepler.ack.AckTimeOut;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim
 *
 * 2016年2月9日
 */
public class AckTimeOutImpl implements AckTimeOut {

	/**
	 * Timeout N次后降级
	 */
	private static final String DEMOTION_KEY = AckTimeOutImpl.class.getName().toLowerCase() + ".demotion";

	private static final int DEMOTION_DEF = PropertiesUtils.get(AckTimeOutImpl.DEMOTION_KEY, Integer.MAX_VALUE);

	private static final Log LOGGER = LogFactory.getLog(AckTimeOutImpl.class);

	private final Profile profile;

	public AckTimeOutImpl(Profile profile) {
		super();
		this.profile = profile;
	}

	public void timeout(ChannelInvoker invoker, Ack ack, long times) {
		int demotion = PropertiesUtils.profile(this.profile.profile(ack.request().service()), AckTimeOutImpl.DEMOTION_KEY, AckTimeOutImpl.DEMOTION_DEF);
		// 当单位超时次数超过指定阀值则Ban(并重连)
		if (times >= demotion) {
			invoker.close();
			AckTimeOutImpl.LOGGER.warn("Host: " + invoker.host() + " baned after " + ack.request().service() + " ( " + ack.request().method() + " ) timeout " + times + " times ... ");
		}
	}
}
