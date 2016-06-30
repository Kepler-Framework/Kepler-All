package com.kepler.zookeeper.blocker;

import com.kepler.service.ServiceInstance;

public interface ServiceInstanceBlocker {
	
	public boolean blocked(ServiceInstance instance);

}
