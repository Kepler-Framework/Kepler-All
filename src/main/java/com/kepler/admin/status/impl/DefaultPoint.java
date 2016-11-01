package com.kepler.admin.status.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.admin.status.Point;

/**
 * @author KimShen
 *
 */
public class DefaultPoint implements Point {

	private static final Log LOGGER = LogFactory.getLog(DefaultPoint.class);

	private static final long serialVersionUID = 1L;

	private final List<Long> times;

	private final List<Long> datas;

	/**
	 * 上限
	 */
	transient private byte max;

	/**
	 * @param max 最大上线
	 */
	public DefaultPoint(byte max) {
		this.times = new ArrayList<Long>(max);
		this.datas = new ArrayList<Long>(max);
		this.max = max;
	}

	@Override
	public List<Long> times() {
		return this.times;
	}

	@Override
	public List<Long> datas() {
		return this.datas;
	}

	/**
	 * 追加数据
	 * 
	 * @param data
	 */
	public void add(long time, long data) {
		// 超过索引立即返回
		if (this.times.size() > this.max) {
			DefaultPoint.LOGGER.warn("Array out of range. [max=" + this.max + "][index=" + this.times.size() + "]");
			return;
		}
		this.times.add(time);
		this.datas.add(data);
	}

	/**
	 * 重置
	 * 
	 * @param begin
	 * @param init
	 */
	public void reset() {
		this.times.clear();
		this.datas.clear();
	}

	public String toString() {
		return "[max=" + this.max + "][index=" + this.datas.size() + "][times=" + this.times + "][datas=" + this.datas + "]";
	}
}
