package com.kepler.generic.reflect.convert.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.ConvertorPriority;
import com.kepler.generic.reflect.convert.ConvertorSelector;

/**
 * @author KimShen
 *
 */
public class DefaultSelector implements ConvertorSelector {

	private static final Log LOGGER = LogFactory.getLog(DefaultSelector.class);

	/**
	 * 用于无法找到任何转换器, 比如String
	 */
	private final Convertor nothing = new NothingConvertor();

	private final List<Convertor> converts;

	public DefaultSelector(List<Convertor> converts) {
		super();
		Collections.sort((this.converts = converts), new ConvertorComparator());
	}

	@Override
	public Convertor select(Class<?> clazz) {
		if (!this.converts.isEmpty()) {
			for (Convertor each : this.converts) {
				if (each.support(clazz)) {
					return each;
				}
			}
		}
		return this.nothing;
	}

	private class ConvertorComparator implements Comparator<Convertor> {

		@Override
		public int compare(Convertor o1, Convertor o2) {
			// 数值越小优先级越高
			return o2.sort() - o1.sort() > 0 ? -1 : 1;
		}
	}

	private class NothingConvertor implements Convertor {

		@Override
		public Object convert(Object source, Class<?> expect, Class<?>[] extension, FieldsAnalyser analyser) throws Exception {
			DefaultSelector.LOGGER.debug("Nothing convertor for source: " + source);
			// 直接返回
			return source;
		}

		@Override
		public boolean support(Class<?> clazz) {
			return false;
		}

		public int sort() {
			return ConvertorPriority.DEFAULT.priority();
		}
	}
}
