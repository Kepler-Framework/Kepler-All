package com.kepler.invoker.impl;

import com.kepler.KeplerLocalException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.invoker.Invoker;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.protocol.Request;

/**
 * @author kim
 *
 * 2016年2月18日
 */
public class DemoteInvoker implements Invoker {

	private static final boolean ACTIVED = PropertiesUtils.get(DemoteInvoker.class.getName().toLowerCase() + ".actived", false);

	/**
	 * 是否需要降级
	 */
	private static final String DEMOTE_KEY = DemoteInvoker.class.getName().toLowerCase() + ".demote";

	private static final boolean DEMOTE_DEF = PropertiesUtils.get(DemoteInvoker.DEMOTE_KEY, false);

	private final MockerContext mocker;

	private final Profile profile;

	public DemoteInvoker(MockerContext mocker, Profile profile) {
		super();
		this.mocker = mocker;
		this.profile = profile;
	}

	@Override
	public boolean actived() {
		return DemoteInvoker.ACTIVED;
	}

	@Override
	public Object invoke(Request request) throws Throwable {
		// 开启Demote则尝试
		return PropertiesUtils.profile(this.profile.profile(request.service()), DemoteInvoker.DEMOTE_KEY, DemoteInvoker.DEMOTE_DEF) ? this.demote(request) : Invoker.EMPTY;
	}

	private Object demote(Request request) {
		Mocker mocker = this.mocker.get(request.service());
		if (mocker != null) {
			return mocker.mock(request);
		}
		throw new KeplerLocalException("Can not found mock service for Service: " + request.service());
	}
}
