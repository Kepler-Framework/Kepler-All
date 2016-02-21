package com.kepler.management.dependency;

import java.util.Set;

import com.kepler.annotation.Service;
import com.kepler.host.Host;

/**
 * @author kim 2015年12月30日
 */
@Service(version = "0.0.1")
public interface Feeder {

	/**
	 * 推送当前主机所依赖的服务
	 * 
	 * @param local
	 * @param dependency
	 */
	public void feed(Host local, Set<Dependency> dependency);
}
