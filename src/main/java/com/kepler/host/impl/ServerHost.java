package com.kepler.host.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerLocalException;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.main.Pid;

/**
 * @author zhangjiehao 2015年7月8日
 */
public class ServerHost implements Serializable, Host {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ServerHost.class);

	private static final int PORT = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".port", 9876);

	/**
	 * 本地端口嗅探范围
	 */
	private static final int RANGE = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".range", 1000);

	/**
	 * 本地端口嗅探间隔
	 */
	private static final int INTERVAL = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".interval", 500);

	/**
	 * 是否校验网卡类型
	 */
	private static final boolean CHECK = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".check", true);

	/**
	 * 是否使用固定端口
	 */
	private static final boolean STABLE = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".stable", false);

	/**
	 * 网卡名称模式
	 */
	private static final String PATTERN = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".pattern", ".*");

	/**
	 * 是否使用主机名替代IP
	 */
	private static final boolean HOSTNAME = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".use_hostname", false);

	/**
	 * IP选择策略
	 */
	private static final Policy POLICY = Policy.valueOf(PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".policy", "V4"));

	/**
	 * 服务唯一ID
	 */
	private static final String SID = PropertiesUtils.get(ServerHost.class.getName().toLowerCase() + ".sid", UUID.randomUUID().toString());

	private final String sid;

	private final Host local;

	private ServerHost(Host host, String sid) {
		this.local = host;
		this.sid = sid;
	}

	public ServerHost(Pid pid) throws Exception {
		this.local = new DefaultHost(Host.LOCATION, Host.GROUP_VAL, Host.TOKEN_VAL, Host.NAME, Host.TAG_VAL, pid.pid(), this.hostname(), ServerHost.STABLE ? ServerHost.PORT : this.available(), Host.FEATURE, Host.PRIORITY_DEF);
		this.sid = ServerHost.SID;
	}

	private String hostname() throws Exception {
		if (ServerHost.HOSTNAME) {
			String hostname = InetAddress.getLocalHost().getHostName();
			ServerHost.LOGGER.info("ServerHost using address: " + hostname);
			return hostname;
		} else {
			ServerHost.LOGGER.info("ServerHost check mode: " + (ServerHost.CHECK ? "[check]" : "[uncheck]"));
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface intr = interfaces.nextElement();
				// 网卡名称是否符合
				Boolean intr_matched = Pattern.matches(ServerHost.PATTERN, intr.getName());
				ServerHost.LOGGER.info("Binding interface name: " + intr.getName() + (intr_matched ? "[matched]" : "[unmatched]"));
				if (intr_matched) {
					Enumeration<InetAddress> addresses = intr.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						if (ServerHost.POLICY.allowed(address) && (!ServerHost.CHECK || (address.isSiteLocalAddress() && !address.isLoopbackAddress() && !address.isLinkLocalAddress()))) {
							ServerHost.LOGGER.info("ServerHost using address: " + address);
							return address.getHostAddress();
						}
					}
				}
				continue;
			}
			ServerHost.LOGGER.warn("Using localhost mode for current service ... ");
			return Host.LOOP;
		}
	}

	/**
	 * 扫描端口
	 * 
	 * @return
	 * @throws Exception
	 */
	private int available() throws Exception {
		for (int index = ServerHost.PORT; index < ServerHost.PORT + ServerHost.RANGE; index++) {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(InetAddress.getByName(Host.LOOP), index), ServerHost.INTERVAL);
			} catch (IOException e) {
				ServerHost.LOGGER.debug("Port " + index + " used ... ");
				return index;
			}
		}
		throw new KeplerLocalException("Cannot allocate port for current service");
	}

	public String sid() {
		return this.sid;
	}

	@Override
	public int port() {
		return this.local.port();
	}

	@Override
	public String pid() {
		return this.local.pid();
	}

	@Override
	public String tag() {
		return this.local.tag();
	}

	@Override
	public String name() {
		return this.local.name();
	}

	@Override
	public String host() {
		return this.local.host();
	}

	@Override
	public String token() {
		return this.local.token();
	}

	@Override
	public String group() {
		return this.local.group();
	}

	public String address() {
		return this.local.address();
	}

	public String location() {
		return this.local.location();
	}

	public int feature() {
		return this.local.feature();
	}

	@Override
	public int priority() {
		return this.local.priority();
	}

	@Override
	public boolean loop(Host host) {
		return this.local.loop(host);
	}

	@Override
	public boolean loop(String host) {
		return this.local.loop(host);
	}

	public int hashCode() {
		return this.local.hashCode();
	}

	public boolean equals(Object ob) {
		// Not null point security
		return this.local.equals(ob);
	}

	public boolean propertyChanged(ServerHost that) {
		return (this.priority() != that.priority()) || (this.tag() == null && that.tag() != null) || (this.tag() != null && !this.tag().equals(that.tag()));
	}

	public String toString() {
		return this.local.toString();
	}

	/**
	 * IP策略
	 * 
	 * @author KimShen
	 *
	 */
	private enum Policy {

		// IPv4 IPv6, 所有
		V4(Inet4Address.class), V6(Inet6Address.class), ALL(null);

		private final Class<? extends InetAddress> inet;

		private Policy(Class<? extends InetAddress> inet) {
			this.inet = inet;
		}

		public boolean allowed(InetAddress inet) {
			// 如果不指定(ALL)直接通过, 否则计算兼容性
			boolean allowed = (this.inet == null ? true : this.inet.isAssignableFrom(inet.getClass()));
			if (!allowed) {
				ServerHost.LOGGER.warn("IP: " + inet + " will be rejected for policy [" + this + "]");
			}
			return allowed;
		}
	}

	public static class Builder {

		private String tag;

		private String sid;

		private String pid;

		private String host;

		private String name;

		private String token;

		private String group;

		private String location;

		private int port;

		private int feature;

		private int priority;

		public Builder(ServerHost that) {
			this.setLocation(that.location()).setFeature(that.feature()).setGroup(that.group()).setToken(that.token()).setName(that.name()).setHost(that.host()).setPid(that.pid()).setPort(that.port()).setPriority(that.priority()).setSid(that.sid()).setTag(that.tag());
		}

		public Builder setFeature(int feature) {
			this.feature = feature;
			return this;
		}

		public Builder setPriority(int priority) {
			this.priority = priority;
			return this;
		}

		public Builder setLocation(String location) {
			this.location = location;
			return this;
		}

		public Builder setGroup(String group) {
			this.group = group;
			return this;
		}

		public Builder setToken(String token) {
			this.token = token;
			return this;
		}

		public Builder setHost(String host) {
			this.host = host;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setTag(String tag) {
			this.tag = tag;
			return this;
		}

		public Builder setPid(String pid) {
			this.pid = pid;
			return this;
		}

		public Builder setSid(String sid) {
			this.sid = sid;
			return this;
		}

		public Builder setPort(int port) {
			this.port = port;
			return this;
		}

		public ServerHost toServerHost() {
			return new ServerHost(new DefaultHost(this.location, this.group, this.token, this.name, this.tag, this.pid, this.host, this.port, this.feature, this.priority), this.sid);
		}
	}
}
