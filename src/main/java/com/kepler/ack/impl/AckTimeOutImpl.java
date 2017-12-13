package com.kepler.ack.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.ack.Ack;
import com.kepler.ack.AckTimeOut;
import com.kepler.channel.ChannelInvoker;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.HostsContext;
import com.kepler.quality.Quality;

/**
 * @author kim
 *
 */
public class AckTimeOutImpl implements AckTimeOut {

	/**
	 * Timeout N次后降级
	 */
	public static final String DEMOTION_KEY = AckTimeOutImpl.class.getName().toLowerCase() + ".demotion";

	private static final int DEMOTION_DEF = PropertiesUtils.get(AckTimeOutImpl.DEMOTION_KEY, Integer.MAX_VALUE);

	private static final boolean CLOSE = PropertiesUtils.get(AckTimeOutImpl.class.getName().toLowerCase() + ".close", true);

	private static final Log LOGGER = LogFactory.getLog(AckTimeOutImpl.class);

	private final ThreadPoolExecutor threads;

	private final HostsContext hosts;

	private final Quality quality;

	private final Profile profile;

	public AckTimeOutImpl(ThreadPoolExecutor threads, HostsContext hosts, Profile profile, Quality quality) {
		super();
		this.profile = profile;
		this.quality = quality;
		this.threads = threads;
		this.hosts = hosts;
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
			AckTimeOutImpl.this.hosts.ban(this.invoker.remote());
			AckTimeOutImpl.this.quality.breaking();
			if (AckTimeOutImpl.CLOSE) {
				this.invoker.close();
			}
			AckTimeOutImpl.LOGGER.warn("Host: " + this.invoker.remote() + " demotion after " + this.ack.request().service() + " ( " + this.ack.request().method() + " ) timeout " + this.times + " times ... ");
		}
	}
}
