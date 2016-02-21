package com.kepler.management.dependency;

import java.io.Serializable;
import java.util.Set;

import com.kepler.host.Host;

/**
 * 主机及其被依赖服务
 * 
 * @author kim 2015年12月30日
 */
public interface Dependency extends Serializable {

	public Host host();

	public Set<DependencyService> services();
}
