package com.kepler.generic.convert.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.extension.Extension;
import com.kepler.generic.analyse.FieldsAnalyser;
import com.kepler.generic.convert.Convertor;
import com.kepler.generic.convert.ConvertorPriority;
import com.kepler.generic.convert.ConvertorSelector;

/**
 * @author KimShen
 *
 */
public class DefaultSelector implements Extension, ConvertorSelector {

	private static final Log LOGGER = LogFactory.getLog(DefaultSelector.class);

	private final Comparator<Convertor> comparator = new ConvertorComparator();

	private final List<Convertor> converts = new ArrayList<Convertor>();

	/**
	 * 用于无法找到任何转换器, 比如String
	 */
	private final Convertor nothing = new NothingConvertor();

	@Override
	public Convertor select(Class<?> clazz) {
		for (Convertor each : this.converts) {
			if (each.support(clazz)) {
				return each;
			}
		}
		return this.nothing;
	}

	@Override
	public DefaultSelector install(Object instance) {
		this.converts.add(Convertor.class.cast(instance));
		// 每次加载后立即排序
		Collections.sort(this.converts, this.comparator);
		return this;
	}

	@Override
	public Class<?> interested() {
		return Convertor.class;
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
