package com.kepler.mock.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.KeplerRoutingException;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.mock.Mocker;
import com.kepler.mock.MockerContext;
import com.kepler.mock.ServiceMocker;
import com.kepler.service.Service;

/**
 * @author kim 2016年1月13日
 */
public class DefaultMockerContext implements ApplicationContextAware, InitializingBean, MockerContext {

	private static final String MOCK_KEY = DefaultMockerContext.class.getName().toLowerCase() + ".mock";

	/**
	 * 是否默认开启Mock
	 */
	private static final boolean MOCK_DEF = PropertiesUtils.get(DefaultMockerContext.MOCK_KEY, false);

	private static final Log LOGGER = LogFactory.getLog(DefaultMockerContext.class);

	private final Map<String, Mocker> mockers = new HashMap<String, Mocker>();

	private final Profile profile;

	private boolean mock = DefaultMockerContext.MOCK_DEF;

	private ApplicationContext applicationContext;

	public DefaultMockerContext(Profile profile) {
		super();
		this.profile = profile;
	}

	/**
	 * 是否默认开启Mock
	 */
	public void mock(boolean mock) {
		this.mock = mock;
	}

	@Override
	public Mocker get(Service service) throws KeplerRoutingException {
		// 如果开启Mock则获取
		return PropertiesUtils.profile(this.profile.profile(service), DefaultMockerContext.MOCK_KEY, this.mock) ? this.getAndWarning(service) : null;
	}

	private Mocker getAndWarning(Service service) {
		Mocker mocker = this.mockers.get(service.service());
		if (mocker != null) {
			Exception exception = new KeplerRoutingException("Using mocker for " + service);
			DefaultMockerContext.LOGGER.error(exception.getMessage(), exception);
		}
		return mocker;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, ServiceMocker> mockers = this.applicationContext.getBeansOfType(ServiceMocker.class);
		for (ServiceMocker mocker : mockers.values()) {
			this.mockers.put(mocker.support().getName(), mocker);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
