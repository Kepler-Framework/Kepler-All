package com.kepler.host;

import java.io.Serializable;
import java.util.Map;

/**
 * @author kim
 *
 * 2016年3月7日
 */
public interface HostStatus extends Serializable {

	public String getSid();

	public String getPid();

	public String getHost();

	public String getGroup();

	public Map<String, Object> getStatus();
}
