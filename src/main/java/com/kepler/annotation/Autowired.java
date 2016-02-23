package com.kepler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否自动发布
 * 
 * @author kim
 *
 * 2016年2月18日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Autowired {

	/**
	 * 实际发布版本, 覆盖@Service. 支持多版本
	 * 
	 * @return
	 */
	String[] version() default "";

	String catalog() default "";

	/**
	 * Profile逻辑名
	 * 
	 * @return
	 */
	String profile() default "";
}
