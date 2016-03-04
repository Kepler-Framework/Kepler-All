package com.kepler.service;

/**
 * 服务依赖
 *
 * @author kim
 *
 * 2016年3月4日
 */
public interface ImportedService {

	public String name();

	public String group();

	public String service();

	public String versionAndCatalog();
}
