package com.kepler.connection.reject;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.KeplerValidateException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Reject;
import com.kepler.extension.Extension;
import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年3月9日
 */
public class DefaultRejectContext implements Extension, Reject {

	public static final String REJECT_KEY = DefaultRejectContext.class.getName().toLowerCase() + ".reject";

	/**
	 * 是否开启
	 */
	private static final boolean ENABLED = PropertiesUtils.get(DefaultRejectContext.class.getName().toLowerCase() + ".enabled", false);

	/**
	 * 默认处理器名称
	 */
	private static final String REJECT_VAL = PropertiesUtils.get(DefaultRejectContext.REJECT_KEY, "default");

	private static final String PREFIX = "rejects";

	private static final Log LOGGER = LogFactory.getLog(DefaultRejectContext.class);

	private final Map<String, Reject> rejects = new HashMap<String, Reject>();

	private final Profile profile;

	public DefaultRejectContext(Profile profile) {
		this.profile = profile;
		// 加载默认处理器
		this.rejects.put(DefaultRejectContext.REJECT_VAL, new NothingReject());
	}

	public DefaultRejectContext install(Object instance) {
		Reject reject = Reject.class.cast(instance);
		this.rejects.put(reject.name(), reject);
		DefaultRejectContext.LOGGER.info("Install Reject: " + reject.name() + " ... ");
		return this;
	}

	@Override
	public String name() {
		return DefaultRejectContext.PREFIX;
	}

	@Override
	public Class<?> interested() {
		return Reject.class;
	}

	@Override
	public void reject(Request request, SocketAddress address) throws KeplerValidateException {
		// 如果开启Reject则获取当前可用Reject, 如果无法获取则使用默认
		// 尝试加载Profile, 如果不存在则使用Default
		if (DefaultRejectContext.ENABLED) {
			this.rejects.get(PropertiesUtils.profile(this.profile.profile(request.service()), DefaultRejectContext.REJECT_KEY, DefaultRejectContext.REJECT_VAL)).reject(request, address);
		}
	}

	/**
	 * 不做任何处理
	 *
	 * @author kim
	 *
	 * 2016年3月9日
	 */
	private class NothingReject implements Reject {

		@Override
		public void reject(Request request, SocketAddress address) throws KeplerValidateException {
		}

		@Override
		public String name() {
			return DefaultRejectContext.REJECT_VAL;
		}
	}
}
