package com.kepler.service.exported;

import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.advised.AdvisedFinder;
import com.kepler.annotation.Queue;
import com.kepler.config.Profile;
import com.kepler.queue.QueueRegister;
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
	private ExportedDelegate(Class<?> service, Object instance, com.kepler.annotation.Service annotation, String profile, Exported exported, Profile profiles, QueueRegister register) {
		this(service, instance, profile, annotation.version(), annotation.catalog(), exported, profiles, register);
	}

	/**
	 * @param service 从@Service获取信息
	 * @param instance
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, Exported exported, Profile profiles, QueueRegister register) {
		this(service, instance, AnnotationUtils.findAnnotation(instance.getClass(), com.kepler.annotation.Service.class), null, exported, profiles, register);
	}

	/**
	 * @param service 从@Service获取信息
	 * @param instance
	 * @param profile
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, String profile, Exported exported, Profile profiles, QueueRegister register) {
		this(service, instance, AnnotationUtils.findAnnotation(instance.getClass(), com.kepler.annotation.Service.class), profile, exported, profiles, register);
	}

	/**
	 * @param service
	 * @param instance
	 * @param profile
	 * @param version 指定Version, 不从@Service获取信息
	 * @param exported
	 * @param profiles
	 */
	public ExportedDelegate(Class<?> service, Object instance, String profile, String version, Exported exported, Profile profiles, QueueRegister register) {
		this(service, instance, profile, version, null, exported, profiles, register);
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
	public ExportedDelegate(Class<?> service, Object instance, String profile, String version, String catalog, Exported exported, Profile profiles, QueueRegister register) {
		super();
		this.service = new Service(service.getName(), version, catalog);
		this.exported = exported;
		this.instance = instance;
		// 先Profile后Register
		profiles.add(this.service, profile);
		register.register(this.service, AdvisedFinder.get(instance, Queue.class));

	}

	public void init() throws Exception {
		this.exported.export(this.service, this.instance);
	}
}
