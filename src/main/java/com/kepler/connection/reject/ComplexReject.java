package com.kepler.connection.reject;

import java.net.SocketAddress;

import com.kepler.KeplerValidateException;
import com.kepler.config.Profile;
import com.kepler.connection.Reject;
import com.kepler.protocol.Request;

/**
 * 混合Service/Address
 * 
 * @author kim
 *
 * 2016年3月9日
 */
public class ComplexReject implements Reject {

	private static final String NAME = "complex";

	private final ServiceReject reject4service;

	private final AddressReject reject4address;

	public ComplexReject(Profile profile) {
		super();
		this.reject4service = new ServiceReject(profile);
		this.reject4address = new AddressReject(profile);
	}

	@Override
	public Request reject(Request request, SocketAddress address) throws KeplerValidateException {
		// 当Service && Address均拒绝才拒绝服务
		try {
			return this.reject4address.reject(request, address);
		} catch (KeplerValidateException exception) {
			return this.reject4service.reject(request, address);
		}
	}

	@Override
	public String name() {
		return ComplexReject.NAME;
	}

}
