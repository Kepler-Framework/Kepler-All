package com.kepler.promotion.impl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.org.apache.commons.collections.map.MultiKeyMap;
import com.kepler.promotion.Promotion;
import com.kepler.protocol.Request;
import com.kepler.service.Exported;
import com.kepler.service.Service;

/**
 * @author kim
 *
 *         2016年2月12日
 */
public class DefaultPromotion implements Exported, Promotion {

	private static final boolean ENABLED = PropertiesUtils.get(DefaultPromotion.class.getName().toLowerCase() + ".enabled", true);

	private static final String ELAPSE_KEY = DefaultPromotion.class.getName().toLowerCase() + ".elapse";

	private static final int ELAPSE_DEF = PropertiesUtils.get(DefaultPromotion.ELAPSE_KEY, 1);

	private static final String TIMES_KEY = DefaultPromotion.class.getName().toLowerCase() + ".times";

	private static final int TIMES_DEF = PropertiesUtils.get(DefaultPromotion.TIMES_KEY, 8);

	private final MultiKeyMap promtions = new MultiKeyMap();

	private final Profile profile;

	public DefaultPromotion(Profile profile) {
		super();
		this.profile = profile;
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		for (Method method : Service.clazz(service).getMethods()) {
			this.promtions.put(service, method.getName(), new Guess());
		}
	}

	@Override
	public boolean promote(Request request) {
		if (DefaultPromotion.ENABLED) {
			Guess guess = Guess.class.cast(this.promtions.get(request.service(), request.method()));
			// 当出现NoSuchMethod时Guess可能为Null
			return guess != null ? guess.guess(request.service()) : false;
		} else {
			// 关闭Promotion则立即返回False
			return false;
		}
	}

	@Override
	public void record(Request request, long start) {
		if (DefaultPromotion.ENABLED) {
			Guess.class.cast(this.promtions.get(request.service(), request.method())).current(request.service(), System.currentTimeMillis() - start);
		}
	}

	private class Guess {

		private final AtomicLong promotion = new AtomicLong();

		/**
		 * @param service
		 * @param elapse
		 */
		public void current(Service service, long elapse) {
			// 如果低于指定耗时则计数+1否则重置
			if (elapse <= PropertiesUtils.profile(DefaultPromotion.this.profile.profile(service), DefaultPromotion.ELAPSE_KEY, DefaultPromotion.ELAPSE_DEF)) {
				this.promotion.incrementAndGet();
			} else {
				this.promotion.set(0);
			}
		}

		public boolean guess(Service service) {
			// 对比次数
			return ((this.promotion.get() >>> 1)) >= PropertiesUtils.profile(DefaultPromotion.this.profile.profile(service), DefaultPromotion.TIMES_KEY, DefaultPromotion.TIMES_DEF);
		}
	}
}
