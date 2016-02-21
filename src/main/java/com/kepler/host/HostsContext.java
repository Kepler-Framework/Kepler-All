package com.kepler.host;

import java.util.Map;

import com.kepler.service.Service;

/**
 * @author kim 2015年7月9日
 */
public interface HostsContext {

	/**
	 * 对所有Hosts进行Ban操作(指定Host所有服务均离线)
	 * 
	 * @param host
	 */
	public void ban(Host host);

	/**
	 * 对所有Hosts进行Active操作(指定Host所有服务均可用)
	 * 
	 * @param host
	 */
	public void active(Host host);

	/**
	 * 仅移除路由, 并不中断连接
	 * 
	 * @param host
	 */
	public void remove(Host host, Service service);

	/**
	 * 获取指定Service的Hosts, 不存在则创建
	 * 
	 * @param service
	 * @return
	 */
	public Hosts getOrCreate(Service service);

	/**
	 * Hosts 4 all service
	 * 
	 * @return
	 */
	public Map<Service, Hosts> hosts();
}
