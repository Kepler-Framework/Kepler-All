package com.kepler.ack.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Ack;
import com.kepler.ack.AckTimeOut;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.quality.Quality;

/**
 * @author kim
 *
 * 2016年2月9日
 */
public class AckTimeOutImpl implements AckTimeOut {

	/**
	 * Timeout N次后降级
	 */
	public static final String DEMOTION_KEY = AckTimeOutImpl.class.getName().toLowerCase() + ".demotion";

	private static final int DEMOTION_DEF = PropertiesUtils.get(AckTimeOutImpl.DEMOTION_KEY, Integer.MAX_VALUE);

	private static final Log LOGGER = LogFactory.getLog(AckTimeOutImpl.class);

	private final ThreadPoolExecutor threads;

	private final Quality quality;

	private final Profile profile;

	public AckTimeOutImpl(ThreadPoolExecutor threads, Profile profile, Quality quality) {
		super();
		this.profile = profile;
		this.quality = quality;
		this.threads = threads;
	}

	public void timeout(ChannelInvoker invoker, Ack ack, long times) {
		// 如果需要熔断则启动异步熔断
		int demotion = PropertiesUtils.profile(this.profile.profile(ack.request().service()), AckTimeOutImpl.DEMOTION_KEY, AckTimeOutImpl.DEMOTION_DEF);
		if (times >= demotion) {
			this.threads.execute(new DemotionRunnable(invoker, times, ack));
		}
	}

	/**
	 * 异步熔断任务
	 * 
	 * @author KimShen
	 *
	 */
	private class DemotionRunnable implements Runnable {

		private final ChannelInvoker invoker;

		private final long times;

		private final Ack ack;

		private DemotionRunnable(ChannelInvoker invoker, long times, Ack ack) {
			super();
			this.invoker = invoker;
			this.times = times;
			this.ack = ack;
		}

		@Override
		public void run() {
			this.invoker.close();
			AckTimeOutImpl.this.quality.breaking();
			AckTimeOutImpl.LOGGER.warn("Host: " + this.invoker.remote() + " baned after " + this.ack.request().service() + " ( " + this.ack.request().method() + " ) timeout " + this.times + " times ... ");
		}
	}
}
