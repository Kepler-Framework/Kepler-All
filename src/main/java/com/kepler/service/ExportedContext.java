package com.kepler.service;

import com.kepler.invoker.Invoker;

/**
 * @author kim 2015年7月8日
 */
public interface ExportedContext {

	/**
	 * Service对应的本地Invoker(代理)
	 * 
	 * @param service
	 * @return
	 */
	public Invoker get(Service service);
}
