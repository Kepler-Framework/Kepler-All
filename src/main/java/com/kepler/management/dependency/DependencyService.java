package com.kepler.management.dependency;

import java.io.Serializable;

/**
 * 被依赖服务
 * 
 * @author kim 2015年12月30日
 */
public interface DependencyService extends Serializable {

	public String service();

	public String versionAndCatalog();
}
