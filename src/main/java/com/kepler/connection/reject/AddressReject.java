package com.kepler.connection.reject;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.kepler.KeplerValidateException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Reject;
import com.kepler.protocol.Request;

/**
 * 地址验证. 如[IP1][IP2][IPn]
 * 
 * @author kim
 *
 * 2016年3月9日
 */
public class AddressReject implements Reject {

	private static final String ADDRESS_KEY = AddressReject.class.getName().toLowerCase() + ".address";

	private static final String ADDRESS_VAL = PropertiesUtils.get(AddressReject.ADDRESS_KEY, "");

	private static final String NAME = "address";

	private final Profile profile;

	public AddressReject(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public Request reject(Request request, SocketAddress address) throws KeplerValidateException {
		String rejects = PropertiesUtils.profile(this.profile.profile(request.service()), AddressReject.ADDRESS_KEY, AddressReject.ADDRESS_VAL);
		// 如果指定服务开启拒绝请求则抛出异常
		if (rejects.matches(".*\\[" + InetSocketAddress.class.cast(address).getHostName() + "\\].*")) {
			throw new KeplerValidateException("Reject: " + request.service() + " from " + address + " ... ");
		}
		return request;
	}

	@Override
	public String name() {
		return AddressReject.NAME;
	}
}
