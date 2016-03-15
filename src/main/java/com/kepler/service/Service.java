package com.kepler.service;

import java.io.Serializable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 服务
 * 
 * @author kim 2015年7月24日
 */
public final class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DEF_CATALOG = "";

	private final Class<?> service;

	private final String version;

	private final String catalog;

	private Service(Class<?> service, com.kepler.annotation.Service annotation) {
		this(service, annotation.version(), annotation.catalog());
	}

	public Service(Class<?> service) {
		this(service, AnnotationUtils.findAnnotation(service, com.kepler.annotation.Service.class));
	}

	public Service(String service, String version) throws Exception {
		this(service, version, null);
	}

	public Service(@JsonProperty("service") String service, @JsonProperty("version") String version, @JsonProperty("catalog") String catalog) throws Exception {
		this(Class.forName(service), version, catalog);
	}

	public Service(Class<?> service, String version) {
		this(service, version, null);
	}

	public Service(Class<?> service, String version, String catalog) {
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
	public Class<?> service() {
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
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object ob) {
		// Not null point security
		Service service = Service.class.cast(ob);
		return this.service().equals(service.service()) && StringUtils.equals(this.version(), service.version()) && StringUtils.equals(this.catalog(), service.catalog());
	}

	public String toString() {
		return "[service]" + this.service.getName() + "[versionAndCatelog]" + this.versionAndCatalog();
	}

	public static String versionAndCatalog(String version, String catalog) {
		return !StringUtils.isEmpty(catalog) ? version + "(" + catalog + ")" : version;
	}
}