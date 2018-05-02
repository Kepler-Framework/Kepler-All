package com.kepler.generic.reflect.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.generic.reflect.GenericBean;

/**
 * @author KimShen
 *
 */
public class DelegateBean implements GenericBean {

	private static final Log LOGGER = LogFactory.getLog(DelegateBean.class);

	private static final long serialVersionUID = 1L;

	@JsonProperty
	private final LinkedHashMap<String, Object> args;

	public DelegateBean() {
		super();
		this.args = new LinkedHashMap<String, Object>();
	}

	public DelegateBean(Map<String, Object> args) {
		this(new LinkedHashMap<String, Object>(args));
	}

	public DelegateBean(@JsonProperty("args") LinkedHashMap<String, Object> args) {
		super();
		this.args = args;
	}

	@SuppressWarnings("unchecked")
	public DelegateBean path(String key) {
		try {
			// 当前取值路径
			StringBuffer path = new StringBuffer();
			for (String each : key.split("\\.")) {
				// Root Path
				if (path.length() == 0) {
					if (!this.args.containsKey(each)) {
						this.args.put(each, new HashMap<String, Object>());
					}
				} else {
					Map<String, Object> inner = Map.class.cast(this.get(path.toString()));
					if (!inner.containsKey(each)) {
						inner.put(each, new HashMap<String, Object>());
					}
				}
				path.append(path.length() == 0 ? each : path.toString() + each);
			}
			return this;
		} catch (Exception e) {
			DelegateBean.LOGGER.info("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	public Object get(String key) {
		String[] keys = key.split("\\.");
		if (keys.length == 1) {
			return this.args.get(key);
		}
		Map<String, Object> current = this.args;
		for (String each : keys) {
			current = Map.class.cast(current.get(each));
		}
		return current;
	}

	public Long getAsLong(String key) {
		return this.getAsLong(key, null);
	}

	public Long getAsLong(String key, Long def) {
		try {
			Object obj = this.get(key);
			return obj != null ? Long.valueOf(obj.toString()) : def;
		} catch (Exception e) {
			DelegateBean.LOGGER.error("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return def;
		}
	}

	public String getAsString(String key) {
		return this.getAsString(key, null);
	}

	public String getAsString(String key, String def) {
		try {
			Object obj = this.get(key);
			return obj != null && !obj.toString().isEmpty() ? obj.toString() : def;
		} catch (Exception e) {
			DelegateBean.LOGGER.error("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return def;
		}
	}

	public Double getAsDouble(String key) {
		return this.getAsDouble(key, null);
	}

	public Double getAsDouble(String key, Double def) {
		try {
			Object obj = this.get(key);
			return obj != null ? Double.valueOf(obj.toString()) : def;
		} catch (Exception e) {
			DelegateBean.LOGGER.error("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return def;
		}
	}

	public Integer getAsInteger(String key) {
		return this.getAsInteger(key, null);
	}

	public Integer getAsInteger(String key, Integer def) {
		try {
			Object obj = this.get(key);
			return obj != null ? Integer.valueOf(obj.toString()) : def;
		} catch (Exception e) {
			DelegateBean.LOGGER.error("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return def;
		}
	}

	public Boolean getAsBoolean(String key) {
		return this.getAsBoolean(key, null);
	}

	public Boolean getAsBoolean(String key, Boolean def) {
		try {
			Object obj = this.get(key);
			return obj != null ? Boolean.valueOf(obj.toString()) : def;
		} catch (Exception e) {
			DelegateBean.LOGGER.error("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return def;
		}
	}

	@SuppressWarnings("unchecked")
	public DelegateBean put(String key, Object value) {
		try {
			String[] keys = key.split("\\.");
			if (keys.length == 1) {
				this.args.put(key, value);
				return this;
			}
			Map<String, Object> current = this.path(key).args;
			for (int index = 0; index < keys.length - 1; index++) {
				current = Map.class.cast(current.get(keys[index]));
			}
			current.put(keys[keys.length - 1], value);
			return this;
		} catch (Exception e) {
			DelegateBean.LOGGER.info("[key=" + key + "][message=" + e.getMessage() + "]", e);
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericBean getAsBean(String key) {
		Map<String, Object> bean = Map.class.cast(this.get(key));
		return bean != null ? new DelegateBean(bean) : null;
	}

	public GenericBean mapping(Map<String, String> mapping) {
		Map<String, Object> bean = new LinkedHashMap<String, Object>();
		for (String key : mapping.keySet()) {
			Object value = this.get(key);
			if (value != null) {
				bean.put(mapping.get(key), value);
			}
		}
		return new DelegateBean(bean);
	}

	@Override
	public LinkedHashMap<String, Object> args() {
		return this.args;
	}

	public String toString() {
		return "[bean][args=" + this.args + "]";
	}
}
