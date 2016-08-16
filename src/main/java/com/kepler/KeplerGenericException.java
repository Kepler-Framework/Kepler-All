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
 * @author kim 2015年8月28日
 */
public class KeplerGenericException extends KeplerLocalException {

	private static final long serialVersionUID = 1L;
	
	private static final Log LOGGER = LogFactory.getLog(KeplerLocalException.class);

	private List<String> throwableClass;
	
	private Map<String, Object> fields;
	
	public KeplerGenericException(Throwable err) {
		super(err);
		throwableClass = new ArrayList<>();
		initThrowableClass(err.getClass());
		initFields(err);
	}
	
	private void initFields(Throwable err) {
		try {
			this.fields = new HashMap<>();
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(err.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor.getReadMethod() == null)
					continue;
				String fieldName = propertyDescriptor.getName();
				if ("class".equals(fieldName)) {
					continue;
				}
				this.fields.put(fieldName, propertyDescriptor.getReadMethod().invoke(err));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void initThrowableClass(Class<?> e) {
		throwableClass.add(e.getName());
		if (e.getSuperclass() != null) {
			initThrowableClass(e.getSuperclass());
		}
	}

	public KeplerGenericException(String reason) {
		super(reason);
	}

	public List<String> getThrowableClass() {
		return throwableClass;
	}

	public void setThrowableClass(List<String> throwableClass) {
		this.throwableClass = throwableClass;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
}
