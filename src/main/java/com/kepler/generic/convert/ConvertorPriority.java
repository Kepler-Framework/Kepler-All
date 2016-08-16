package com.kepler.generic.convert;

/**
 * @author KimShen
 *
 */
public enum ConvertorPriority {

	DEFAULT(100), HIGH(50), LOW(150);

	private int priority;

	private ConvertorPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * 获取优先级
	 * 
	 * @return
	 */
	public int priority() {
		return this.priority;
	}
}
