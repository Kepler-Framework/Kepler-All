package com.kepler;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zhangjiehao 2016年8月16日
 */
public class KeplerGenericException extends KeplerLocalException {

	private static final Log LOGGER = LogFactory.getLog(KeplerGenericException.class);

	private static final long serialVersionUID = 1L;

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
				// 忽略GetClass
				String fieldName = propertyDescriptor.getName();
				if ("class".equals(fieldName)) {
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