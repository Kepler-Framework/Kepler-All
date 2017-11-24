package com.kepler;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.kepler.config.PropertiesUtils;

/**
 * 泛化调用错误
 * 
 * @author zhangjiehao 2016年8月16日
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public class KeplerGenericException extends KeplerLocalException implements Cloneable {

	private static final String FILTER = PropertiesUtils.get(KeplerGenericException.class.getName().toLowerCase() + ".filter", "cause;message;stackTrace;suppressed;localizedMessage;");

	private static final Boolean STACK = PropertiesUtils.get(KeplerGenericException.class.getName().toLowerCase() + ".stack", false);

	private static final Set<Class<? extends Throwable>> FILTER_CLASS = new HashSet<Class<? extends Throwable>>();

	private static final Log LOGGER = LogFactory.getLog(KeplerGenericException.class);

	private static final Set<String> FILTER_FIELD = new HashSet<String>();

	private static final long serialVersionUID = 1L;

	static {
		// 追加过滤属性
		for (String field : KeplerGenericException.FILTER.split(";")) {
			KeplerGenericException.FILTER_FIELD.add(field);
		}
		KeplerGenericException.FILTER_CLASS.add(Exception.class);
		KeplerGenericException.FILTER_CLASS.add(Throwable.class);
		KeplerGenericException.FILTER_CLASS.add(RuntimeException.class);
	}

	/**
	 * 用来保存原始异常属性
	 */
	private Map<String, Object> fields;

	/**
	 * 用来保存原始异常类型
	 */
	private List<String> classes;

	/**
	 * 文本错误
	 */
	private String reason;

	public KeplerGenericException() {
		this("");
	}

	public KeplerGenericException(Throwable throwable) {
		this(KeplerGenericException.STACK ? KeplerGenericException.cause(throwable) : "[exception=" + throwable.getClass() + "][message=" + throwable.getMessage() + "]");
		this.fields = new HashMap<String, Object>();
		this.classes = new ArrayList<String>();
		this.classes(throwable.getClass());
		this.fields(throwable);
		this.reason = null;
	}

	public KeplerGenericException(String reason) {
		super(reason);
		this.reason = reason;
		this.classes = null;
		this.fields = null;
	}

	private static String cause(Throwable throwable) {
		try (StringWriter cause = new StringWriter()) {
			PrintWriter printer = new PrintWriter(cause);
			throwable.printStackTrace(printer);
			return cause.toString();
		} catch (IOException e) {
			KeplerGenericException.LOGGER.warn(e.getMessage(), e);
			return "";
		}
	}

	/**
	 * 采集异常
	 * 
	 * @param throwable
	 */
	private void classes(Class<?> throwable) {
		this.classes.add(throwable.getName());
		// 如果未被阻断则继续解析
		if (!KeplerGenericException.FILTER_CLASS.contains(throwable.getSuperclass())) {
			this.classes(throwable.getSuperclass());
		}
	}

	/**
	 * 解析异常属性
	 * 
	 * @param throwable
	 */
	private void fields(Throwable throwable) {
		try {
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(throwable.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				// Guard case1, 仅处理Get/Is
				if (descriptor.getReadMethod() == null) {
					continue;
				}
				String field = descriptor.getName();
				// Guard case2, 忽略GetClass及过滤属性
				if ("class".equals(field) || KeplerGenericException.FILTER_FIELD.contains(field)) {
					continue;
				}
				this.fields.put(field, descriptor.getReadMethod().invoke(throwable));
			}
		} catch (Exception e) {
			KeplerGenericException.LOGGER.error(e.getMessage(), e);
		}
	}

	public KeplerGenericException clone() {
		// 克隆, 如果存在Field则使用其字符串形式, 如果不存在Field则检查Reason
		return new KeplerGenericException(this.fields != null ? this.fields.toString() : StringUtils.isEmpty(this.reason) ? this.reason : "unknow");
	}

	public List<String> getThrowableClass() {
		return this.classes;
	}

	public Map<String, Object> getFields() {
		return this.fields;
	}
}