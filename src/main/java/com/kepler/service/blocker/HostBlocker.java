package com.kepler.service.blocker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.service.InstanceBlocker;
import com.kepler.service.ServiceInstance;

/**
 * 根据IP地址进行阻断通知
 * 
 * @author longyaokun
 * 
 * @date 2016年6月30日
 */
public class HostBlocker implements InstanceBlocker {

	private static final Log LOGGER = LogFactory.getLog(HostBlocker.class);
	
	/**
	 * Format:[ip1][ip2]
	 */
	private static final String BLOCKED_IP = PropertiesUtils.get(HostBlocker.class.getName().toLowerCase() + ".blocked_ip", "");

	@Override
	public boolean blocked(ServiceInstance instance) {
		if (HostBlocker.BLOCKED_IP.matches(".*\\[" + instance.host().host() + "\\].*")) {
			HostBlocker.LOGGER.warn("Block the server host [" + instance.host().host() + "] from been connected");
			return true;
		}
		return false;
	}

}
