package com.kepler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author kim 2016年1月15日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ForkJoin {

	/**
	 * Fork策略逻辑名称
	 * 
	 * @return
	 */
	public String fork();

	/**
	 * Join策略逻辑名称
	 * 
	 * @return
	 */
	public String join();
}
