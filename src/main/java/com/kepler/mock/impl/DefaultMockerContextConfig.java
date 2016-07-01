package com.kepler.mock.impl;

import com.kepler.annotation.Config;
import com.kepler.annotation.Internal;

/**
 * @see com.kepler.thread.ThreadFactoryConfig
 * 
 * @author kim 2016年1月13日
 */
@Internal
public class DefaultMockerContextConfig {

	private final DefaultMockerContext context;

	public DefaultMockerContextConfig(DefaultMockerContext context) {
		super();
		this.context = context;
	}

	@Config(name = "com.kepler.mock.impl.defaultmockercontext.mock")
	public void mock(boolean mock) {
		this.context.mock(mock);
	}
}
