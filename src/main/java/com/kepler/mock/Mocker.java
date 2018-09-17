package com.kepler.mock;

import com.kepler.protocol.Request;

import java.lang.reflect.Method;

/**
 * @author kim 2016年1月13日
 */
public interface Mocker {

    Object mock(Request request, Method method) throws Exception;

}
