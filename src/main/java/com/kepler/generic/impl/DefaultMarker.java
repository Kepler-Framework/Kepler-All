package com.kepler.generic.impl;

import com.kepler.generic.GenericMarker;
import com.kepler.generic.reflect.impl.DefaultDelegate;
import com.kepler.header.Headers;

/**
 * @author KimShen
 *
 */
public class DefaultMarker implements GenericMarker {

	@Override
	public Headers mark(Headers headers) {
		// 泛化标记, 不调整KEY位置以兼容
		return headers.put(DefaultDelegate.DELEGATE_KEY, DefaultDelegate.DELEGATE_VAL);
	}
}
