package com.kepler.invoker.forkjoin.impl;

import java.util.Collection;
import java.util.Map;

import com.kepler.invoker.forkjoin.Joiner;

/**
 * @author kim 2016年1月16日
 */
public class Joiner4Complex implements Joiner {

	public static final String NAME = "complex";

	@SuppressWarnings("unchecked")
	private Object add(Object current, Object joined) {
		Collection.class.cast(current).addAll(Collection.class.cast(joined));
		return current;
	}

	@SuppressWarnings("unchecked")
	private Object put(Object current, Object joined) {
		Map.class.cast(current).putAll(Map.class.cast(joined));
		return current;
	}

	@Override
	public Object join(Object current, Object joined) {
		// Guard case, 如果Joined结果为Null则不做处理
		if (joined == null) {
			return current;
		}
		// 如果为Collection类型则调用Add, 否则调用Put. 如果Current为Null则直接返回新值
		return current != null ? Collection.class.isAssignableFrom(joined.getClass()) ? this.add(current, joined) : this.put(current, joined) : joined;
	}

	@Override
	public String name() {
		return Joiner4Complex.NAME;
	}
}
