package com.kepler.generic.reflect;

import java.util.LinkedHashMap;

import com.kepler.service.Service;

/**
 * 泛化调用代理
 * 
 * @author KimShen
 *
 */
public interface GenericService {

	public Object invoke(Service service, String method, LinkedHashMap<String, Object> args) throws Throwable;

	public Object invoke(Service service, String method, String[] classes, Object... args) throws Throwable;

	public Object invoke(Service service, String method, GenericBean bean) throws Throwable;

	public Object invoke(Service service, String method, GenericArgs args) throws Throwable;
	
	public GenericBean invokeAsBean(Service service, String method, LinkedHashMap<String, Object> args) throws Throwable;

	public GenericBean invokeAsBean(Service service, String method, String[] classes, Object... args) throws Throwable;

	public GenericBean invokeAsBean(Service service, String method, GenericBean bean) throws Throwable;

	public GenericBean invokeAsBean(Service service, String method, GenericArgs args) throws Throwable;
}
