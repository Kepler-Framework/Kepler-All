package com.kepler.service;

import java.io.Serializable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * 服务
 * 
 * @author kim 2015年7月24日
 */
public final class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DEF_CATALOG = "";

	private final String service;

	private final String version;

	private final String catalog;

	private Service(String service, com.kepler.annotation.Service annotation) throws Exception {
		this(service, annotation.version(), annotation.catalog());
	}

	public Service(Class<?> service) throws Exception {
		this(service.getName(), AnnotationUtils.findAnnotation(service, com.kepler.annotation.Service.class));
	}

	public Service(String service, String version) throws Exception {
		this(service, version, null);
	}

	public Service(@JsonProperty("service") String service, @JsonProperty("version") String version, @JsonProperty("catalog") String catalog) {
		super();
		this.service = service;
		this.version = version.trim();
		// Catalog为Null或""则使用默认值
		this.catalog = StringUtils.isEmpty(catalog) ? Service.DEF_CATALOG : catalog.trim();
		Assert.notNull(this.service(), "Class " + service + " can't found service");
		// Version校验为是否包含值
		Assert.hasText(this.version(), "Class " + service + " can't found version");
	}

	@JsonProperty
	public String service() {
		return this.service;
	}

	@JsonProperty
	public String version() {
		return this.version;
	}

	@JsonProperty
	public String catalog() {
		return this.catalog;
	}

	public String versionAndCatalog() {
		return Service.versionAndCatalog(this.version(), this.catalog());
	}

	public int hashCode() {
		return this.service().hashCode() ^ this.versionAndCatalog().hashCode();
	}

	public boolean equals(Object ob) {
		if (ob == null) {
			return false;
		}
		Service service = Service.class.cast(ob);
		return StringUtils.equals(this.service(), service.service()) && StringUtils.equals(this.version(), service.version()) && StringUtils.equals(this.catalog(), service.catalog());
	}

	public String toString() {
		return "[Service](" + this.service + ")[VersionAndCatelog](" + this.versionAndCatalog() + ")";
	}

	/**
	 * 获取Service对应的Class
	 */
	public static Class<?> clazz(Service service) throws Exception {
		return Class.forName(service.service());
	}

	public static String versionAndCatalog(String version, String catalog) {
		return !StringUtils.isEmpty(catalog) ? version + "(" + catalog + ")" : version;
	}
}