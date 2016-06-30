package com.kepler.zookeeper.blocker.impl;

import java.util.List;

import com.kepler.service.ServiceInstance;
import com.kepler.zookeeper.blocker.ServiceInstanceBlocker;

/**
 * ZK节点变更通知阻断器
 * 
 * @author longyaokun
 * @date 2016年6月30日
 */
public class DefaultServiceInstanceBlocker implements ServiceInstanceBlocker {

	private final List<ServiceInstanceBlocker> blockers;

	public DefaultServiceInstanceBlocker(List<ServiceInstanceBlocker> blockers) {
		this.blockers = blockers;
	}

	@Override
	public boolean blocked(ServiceInstance instance) {
		for (ServiceInstanceBlocker blocker : this.blockers) {
			if (blocker.blocked(instance)) {
				return true;
			}
		}
		return false;
	}

}
