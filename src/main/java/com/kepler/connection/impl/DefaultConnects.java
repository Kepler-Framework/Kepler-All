package com.kepler.connection.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.ConnectHost;
import com.kepler.connection.Connects;
import com.kepler.host.Host;

/**
 * 延迟重连
 * 
 * @author kim 2015年7月10日
 */
public class DefaultConnects implements Connects {

	private static final int INTERVAL = PropertiesUtils.get(DefaultConnects.class.getName().toLowerCase() + ".interval", 60000);

	private static final int DELAY = PropertiesUtils.get(DefaultConnects.class.getName().toLowerCase() + ".delay", 5000);

	private static final Log LOGGER = LogFactory.getLog(DefaultConnects.class);

	private final BlockingQueue<ConnectHost> queue = new DelayQueue<ConnectHost>();

	@Override
	public Host get() throws Exception {
		ConnectHost connect = this.queue.poll(DefaultConnects.INTERVAL, TimeUnit.MILLISECONDS);
		return connect != null ? connect.host() : null;
	}

	@Override
	public void put(Host host) {
		DelayConnectHost connect = new DelayConnectHost(host);
		this.queue.offer(connect);
		DefaultConnects.LOGGER.warn("Host: " + connect + " will reconnect after " + DefaultConnects.DELAY);
	}

	private class DelayConnectHost implements ConnectHost {

		/**
		 * 当前时间 + delay时间
		 */
		private final long deadline = TimeUnit.MILLISECONDS.convert(DefaultConnects.DELAY, TimeUnit.MILLISECONDS) + System.currentTimeMillis();

		private final Host host;

		private DelayConnectHost(Host host) {
			super();
			this.host = host;
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		public int compareTo(Delayed o) {
			return this.getDelay(TimeUnit.SECONDS) >= o.getDelay(TimeUnit.SECONDS) ? 1 : -1;
		}

		@Override
		public Host host() {
			return this.host;
		}

		public String toString() {
			return "[deadline=" + this.deadline + "][host=" + this.host + "]";
		}
	}
}
