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
		// 是否Header中标记了泛化
		if (headers != null && StringUtils.equals(headers.get(this.key()), this.value())) {
			// 如果标记则清空Header防止调用链错误
			headers.put(this.key(), null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Headers mark(Headers headers) {
		return headers.put(this.key(), this.value());
	}

	abstract protected String key();

	abstract protected String value();
}
