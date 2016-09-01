package com.kepler.generic.impl;

import com.kepler.generic.GenericMarker;
import com.kepler.header.Headers;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public abstract class DefaultMarker implements GenericMarker {

	@Override
	public boolean marked(Headers headers) {
		// 是否Header中标记了泛化并且符合Token
		if (headers != null && StringUtils.equals(headers.get(this.key()), this.value())) {
			// 如果标记则清空Header防止调用链错误
			headers.delete(this.key());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Headers mark(Headers headers) {
		// 泛化标记
		return headers.put(this.key(), this.value());
	}

	abstract protected String key();

	abstract protected String value();
}
