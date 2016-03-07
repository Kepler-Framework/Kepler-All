package com.kepler.service;

import java.io.Serializable;

/**
 * 服务依赖
 *
 * @author kim
 *
 * 2016年3月4日
 */
public interface ImportedService extends Serializable {

	public String sid();

	public String name();

	public String group();

	public String service();

	public String versionAndCatalog();
}
