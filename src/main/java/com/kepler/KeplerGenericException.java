package com.kepler;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;

/**
 * @author zhangjiehao 2016年8月16日
 */
public class KeplerGenericException extends KeplerLocalException {

	private static final String FILTER = PropertiesUtils.get(KeplerGenericException.class.getName().toLowerCase() + ".filter", "stackTrace");

	private static final Log LOGGER = LogFactory.getLog(KeplerGenericException.class);

	private static final Set<String> FILTER_FIELD = new HashSet<String>();

	private static final long serialVersionUID = 1L;

	static {
		// 追加过滤属性
		for (String field : KeplerGenericException.FILTER.split(",")) {
			KeplerGenericException.FILTER_FIELD.add(field);
		}
	}

	private Map<String, Object> fields;

	private List<String> classes;

	public KeplerGenericException(Throwable throwable) {
		super(throwable);
		this.fields = new HashMap<String, Object>();
		this.classes = new ArrayList<String>();
		this.initClasses(throwable.getClass());
		this.initFields(throwable);
	}

	public KeplerGenericException(String reason) {
		super(reason);
	}

	private void initClasses(Class<?> throwable) {
		this.classes.add(throwable.getName());
		// 没有父类并且到Throwable终止
		if (throwable.getSuperclass() != null && !Throwable.class.equals(throwable)) {
			this.initClasses(throwable.getSuperclass());
		}
	}

	private void initFields(Throwable throwable) {
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(throwable.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				// 仅处理Get/Is
				if (propertyDescriptor.getReadMethod() == null)
					continue;
				String fieldName = propertyDescriptor.getName();
				// 忽略GetClass及过滤属性
				if ("class".equals(fieldName) || KeplerGenericException.FILTER_FIELD.contains(fieldName)) {
					continue;
				}
				this.fields.put(fieldName, propertyDescriptor.getReadMethod().invoke(throwable));
			}
		} catch (Exception e) {
			KeplerGenericException.LOGGER.error(e.getMessage(), e);
		}
	}

	public List<String> getThrowableClass() {
		return this.classes;
	}

	public Map<String, Object> getFields() {
		return this.fields;
	}
}