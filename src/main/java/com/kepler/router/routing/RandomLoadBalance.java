package com.kepler.router.routing;

import java.util.concurrent.ThreadLocalRandom;

import com.kepler.config.Profile;
import com.kepler.router.Routing;

/**
 * @author zhangjiehao 2015年9月7日
 */
public class RandomLoadBalance extends LoadBalance {

	public RandomLoadBalance(Profile profile) {
		super(profile);
	}

	@Override
	public String name() {
		return Routing.NAME;
	}

	@Override
	protected int next(int weights) {
		return weights == 0 ? weights : ThreadLocalRandom.current().nextInt(weights);
	}
}
