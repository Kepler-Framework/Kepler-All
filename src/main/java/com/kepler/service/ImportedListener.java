package com.kepler.service;

/**
 * 节点变化通知
 * 
 * @author zhangjiehao 2015年11月9日
 */
public interface ImportedListener {

	public void add(ServiceInstance instance) throws Exception;

	public void delete(ServiceInstance instance) throws Exception;

	public void change(ServiceInstance current, ServiceInstance newInstance) throws Exception;
}