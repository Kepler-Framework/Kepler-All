package com.kepler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数回调
 * 
 * @author kim 2015年12月27日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Config {

	public String value();

	/**
	 * 是否启动时初始化
	 * 
	 * @return
	 */
	public boolean init() default false;
}
