package com.kepler.mock;

import com.kepler.protocol.Request;

/**
 * @author kim 2016年1月13日
 */
public interface Mocker {

    Object mock(Request request) throws Exception;

}
