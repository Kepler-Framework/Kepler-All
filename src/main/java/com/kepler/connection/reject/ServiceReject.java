package com.kepler.connection.reject;

import java.net.SocketAddress;

import com.kepler.KeplerValidateException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Reject;
import com.kepler.protocol.Request;

/**
 * 指定服务是否拒绝访问
 * 
 * @author kim
 *
 * 2016年3月9日
 */
public class ServiceReject implements Reject {

	public static final String REJECT_KEY = ServiceReject.class.getName().toLowerCase() + ".reject";

	private static final boolean REJECT_VAL = PropertiesUtils.get(ServiceReject.REJECT_KEY, false);

	private static final String NAME = "service";

	private final Profile profile;

	public ServiceReject(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public void reject(Request request, SocketAddress address) throws KeplerValidateException {
		// 如果指定服务开启拒绝请求则抛出异常
		if (PropertiesUtils.profile(this.profile.profile(request.service()), ServiceReject.REJECT_KEY, ServiceReject.REJECT_VAL)) {
			throw new KeplerValidateException("Reject: " + request.service() + " from " + address + " ... ");
		}
	}

	@Override
	public String name() {
		return ServiceReject.NAME;
	}
}
