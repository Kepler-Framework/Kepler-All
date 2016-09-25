package com.kepler.generic;

import com.kepler.KeplerGenericException;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface GenericDelegate {

	/**
	 * 泛化调用
	 * 
	 * @param instance
	 * @param method
	 * @param request
	 * @return
	 * @throws KeplerGenericException
	 */
	public GenericResponse delegate(Object instance, String method, Request request) throws KeplerGenericException;
}
