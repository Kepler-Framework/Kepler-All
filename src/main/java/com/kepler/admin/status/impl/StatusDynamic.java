package com.kepler.admin.status.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;

/**
 * @author KimShen
 *
 */
abstract class StatusDynamic implements Status {

	volatile protected Map<String, Object> status_one = new HashMap<String, Object>();

	volatile protected Map<String, Object> status_two = new HashMap<String, Object>();

	private final String[] fields;

	protected StatusDynamic(String[] fields) {
		super();
		this.fields = fields;
		this.init(this.status_one);
		this.init(this.status_two);
	}

	@Override
	public Map<String, Object> get() {
		// 交换并重置缓存
		Map<String, Object> current = this.status_one;
		this.status_one = this.reset(this.status_two);
		this.status_two = current;
		return current;
	}

	/**
	 * 初始化
	 * 
	 * @param status
	 */
	protected void init(Map<String, Object> status) {
		for (String field : this.fields) {
			status.put(field, new DefaultPoint(this.max()));
		}
	}

	/**
	 * 追加数据
	 * 
	 * @param field
	 * @param time
	 * @param data
	 */
	protected void add(String field, long time, long data) {
		DefaultPoint.class.cast(this.status_one.get(field)).add(time, data);
	}

	/**
	 * 重置
	 * 
	 * @param status
	 */
	protected Map<String, Object> reset(Map<String, Object> status) {
		for (String field : this.fields) {
			DefaultPoint.class.cast(status.get(field)).reset();
		}
		return status;
	}

	abstract protected byte max();
}
