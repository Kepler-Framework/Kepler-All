package com.kepler.main;

/**
 * 启动前调用
 *
 * @author kim 2015年9月15日
 */
public interface Prepare {

    String CLASS = System.getProperty(Prepare.class.getName().toLowerCase() + ".class", null);

    void prepare() throws Exception;
}
