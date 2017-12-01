package com.kepler.service;

import java.util.List;

/**
 * @author KimShen
 *
 */
public interface ExportedInfo {
	
	public List<ServiceInstance> instance() throws Exception;

	public List<Service> services() throws Exception;
}
