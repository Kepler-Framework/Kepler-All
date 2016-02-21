package com.kepler.service.exported;

import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.config.Profile;
import com.kepler.service.Exported;
import com.kepler.service.Service;

/**
 * 代理发布
 * 
 * @author kim 2015年7月8日
 */
public class ExportedDelegate {

	private final Exported exported;

	private final Service service;

	private final Object instance;

	/**
	 * @param service
	 * @param instance
	 * @param annotation
	 * @param profile
	 * @param exported
	 * @param profiles
	 */
	private ExportedDelegate(Class<?> service, Object instance, com.kepler.annotation.Service annotation, String profile, Exported exported, Profile profiles) {
		this(service, instance, profile, annotation.version(), annotation.catalog(), exported, profiles);
	}

	/**
	 * @param service 从@Service获取信息
	 * @param instance
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, Exported exported, Profile profiles) {
		this(service, instance, AnnotationUtils.findAnnotation(instance.getClass(), com.kepler.annotation.Service.class), null, exported, profiles);
	}

	/**
	 * @param service 从@Service获取信息
	 * @param instance
	 * @param profile
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, String profile, Exported exported, Profile profiles) {
		this(service, instance, AnnotationUtils.findAnnotation(instance.getClass(), com.kepler.annotation.Service.class), profile, exported, profiles);
	}

	/**
	 * @param service
	 * @param instance
	 * @param profile
	 * @param version 指定Version, 不从@Service获取信息
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, String profile, String version, Exported exported, Profile profiles) {
		this(service, instance, profile, version, null, exported, profiles);
	}

	/**
	 * @param service
	 * @param instance
	 * @param profile
	 * @param version
	 * @param catalog
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, String profile, String version, String catalog, Exported exported, Profile profiles) {
		super();
		this.exported = exported;
		this.instance = instance;
		this.service = new Service(service, version, catalog);
		profiles.add(this.service, profile);
	}

	public void init() throws Exception {
		this.exported.exported(this.service, this.instance);
	}
}
