package com.kepler.service;

import java.io.Serializable;

import com.kepler.host.impl.ServerHost;

/**
 * 服务节点
 * 
 * @author zhangjiehao 2015年11月9日
 */
public interface ServiceInstance extends Serializable {

	public ServerHost host();

	public String service();

	public String version();

	public String catalog();

	public String versionAndCatalog();
}
