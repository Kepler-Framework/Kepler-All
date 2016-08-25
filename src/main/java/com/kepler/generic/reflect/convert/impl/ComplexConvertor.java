package com.kepler.generic.reflect.convert.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.Getter;

/**
 * @author KimShen
 *
 */
abstract class ComplexConvertor implements Convertor {

	/**
	 * 集合或数组转换为Getter
	 * 
	 * @param source 数据源
	 * @param extension 目标类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Getter getter(Object source) {
		return Collection.class.isAssignableFrom(source.getClass()) ? new CollectionGetter(Collection.class.cast(source)) : new ArrayGetter(source);
	}

	/**
	 * 处理数组的Getter
	 * 
	 * @author KimShen
	 *
	 */
	private class ArrayGetter implements Getter {

		private final Object source;

		private int index;

		private ArrayGetter(Object source) {
			super();
			this.source = source;
		}

		public boolean empty() {
			return Array.getLength(this.source) == 0;
		}

		@Override
		public Object next() {
			try {
				return Array.get(this.source, this.index);
			} finally {
				this.index++;
			}
		}

		public int length() {
			return Array.getLength(this.source);
		}
	}

	/**
	 * 处理集合的Getter
	 * 
	 * @author KimShen
	 *
	 */
	private class CollectionGetter implements Getter {

		private final Iterator<Object> iterator;

		private final int length;

		private CollectionGetter(Collection<Object> collection) {
			super();
			this.iterator = collection.iterator();
			this.length = collection.size();

		}

		public boolean empty() {
			return this.length == 0;
		}

		@Override
		public Object next() {
			return this.iterator.next();
		}

		public int length() {
			return this.length;
		}
	}
}
