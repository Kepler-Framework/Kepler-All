package com.kepler.service;

/**
 * 阻断器
 * 
 * @author longyaokun
 *
 */
public interface InstanceBlocker {

	/**
	 * @param instance 服务实例
	 * 
	 * @return 返回False表示不进行连接
	 */
	public boolean blocked(ServiceInstance instance);
}
