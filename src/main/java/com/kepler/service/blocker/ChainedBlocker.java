package com.kepler.service.blocker;

import java.util.List;

import com.kepler.service.InstanceBlocker;
import com.kepler.service.ServiceInstance;

/**
 * ZK节点变更通知阻断器责任链
 * 
 * @author longyaokun
 * 
 * @date 2016年6月30日
 */
public class ChainedBlocker implements InstanceBlocker {

	private final List<InstanceBlocker> blockers;

	public ChainedBlocker(List<InstanceBlocker> blockers) {
		this.blockers = blockers;
	}

	@Override
	public boolean blocked(ServiceInstance instance) {
		if (!this.blockers.isEmpty()) {
			for (InstanceBlocker blocker : this.blockers) {
				if (blocker.blocked(instance)) {
					return true;
				}
			}
		}
		return false;
	}
}
