package com.kepler.service.imported;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.kepler.header.HeadersProcessor;
import com.kepler.id.IDGenerator;
import com.kepler.invoker.Invoker;
import com.kepler.protocol.RequestFactory;
import com.kepler.protocol.RequestValidation;
import com.kepler.serial.SerialID;
import com.kepler.serial.Serials;
import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class ImportedServiceFactory<T> implements FactoryBean<T> {

	private final CglibProxy proxy = new CglibProxy();

	private final RequestValidation validation;

	private final HeadersProcessor processor;

	private final RequestFactory factory;

	private final HeadersContext header;

	private final IDGenerator generator;

	private final List<Method> methods;

	private final Imported imported;

	private final Profile profile;

	private final Serials serials;

	private final Service service;

	private final Invoker invoker;

	private ImportedServiceFactory(Class<T> clazz, com.kepler.annotation.Service service, String profile, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		this(clazz, profile, service.version(), service.catalog(), invoker, validation, factory, header, processor, generator, profiles, serials, imported);
	}

	private ImportedServiceFactory(Class<T> clazz, com.kepler.annotation.Service service, String profile, String catalog, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		this(clazz, profile, service.version(), catalog, invoker, validation, factory, header, processor, generator, profiles, serials, imported);
	}

	// 使用@Service获取信息, 并使用默认Profile
	public ImportedServiceFactory(Class<T> clazz, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		this(clazz, AnnotationUtils.findAnnotation(clazz, com.kepler.annotation.Service.class), null, invoker, validation, factory, header, processor, generator, profiles, serials, imported);
	}

	// 使用@Service获取信息, 并指定Profile
	public ImportedServiceFactory(Class<T> clazz, String profile, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		this(clazz, AnnotationUtils.findAnnotation(clazz, com.kepler.annotation.Service.class), profile, invoker, validation, factory, header, processor, generator, profiles, serials, imported);
	}

	// 使用@Service获取信息, 并指定Profile和Catalog
	public ImportedServiceFactory(Class<T> clazz, String profile, String catalog, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		this(clazz, AnnotationUtils.findAnnotation(clazz, com.kepler.annotation.Service.class), profile, catalog, invoker, validation, factory, header, processor, generator, profiles, serials, imported);
	}

	// 不使用@Service
	public ImportedServiceFactory(Class<T> clazz, String profile, String version, String catalog, Invoker invoker, RequestValidation validation, RequestFactory factory, HeadersContext header, HeadersProcessor processor, IDGenerator generator, Profile profiles, Serials serials, Imported imported) {
		super();
		this.header = header;
		this.invoker = invoker;
		this.factory = factory;
		this.serials = serials;
		this.imported = imported;
		this.generator = generator;
		this.processor = processor;
		this.validation = validation;
		this.methods = Arrays.asList(clazz.getMethods());
		this.service = new Service(clazz, version, catalog);
		this.profile = profiles.add(this.service, profile);
	}

	public T getObject() throws Exception {
		this.imported.subscribe(this.service);
		return this.proxy.getProxy(this.service.service());
	}

	public Class<?> getObjectType() {
		return this.service.service();
	}

	public boolean isSingleton() {
		return true;
	}

	private class CglibProxy implements MethodInterceptor {

		private final Enhancer enhancer = new Enhancer();

		@SuppressWarnings("unchecked")
		public T getProxy(Class<?> clazz) {
			this.enhancer.setCallback(this);
			this.enhancer.setSuperclass(clazz);
			return (T) this.enhancer.create();
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			// 如果为Service(Interface)方法则使用代理
			return ImportedServiceFactory.this.methods.contains(method) ? this.invoke(method, args) : proxy.invokeSuper(obj, args);
		}

		private Object invoke(Method method, Object[] args) throws Throwable {
			// 从当前上下文获取Headers并进行合并
			Headers headers = Headers.ENABLED ? ImportedServiceFactory.this.processor.process(ImportedServiceFactory.this.service, ImportedServiceFactory.this.header.release()) : null;
			// PropertiesUtils.profile(ImportedServiceFactory.this.profile.profile(service), SerialID.Serial.SERIAL_KEY, SerialID.Serial.SERIAL_VAL)), 获取与Service相关的序列化策略, 并将String转换为对应Byte
			byte serial = SerialID.DYAMIC ? ImportedServiceFactory.this.serials.output(PropertiesUtils.profile(ImportedServiceFactory.this.profile.profile(ImportedServiceFactory.this.service), SerialID.SERIAL_KEY, SerialID.SERIAL_VAL)) : ImportedServiceFactory.this.serials.output(SerialID.SERIAL_VAL);
			// 如果返回类型为Future(Future.class.isAssignableFrom(method.getReturnType()))则标记为Async
			return ImportedServiceFactory.this.invoker.invoke(ImportedServiceFactory.this.validation.valid(ImportedServiceFactory.this.factory.request(headers, ImportedServiceFactory.this.service, method, Future.class.isAssignableFrom(method.getReturnType()), args, ImportedServiceFactory.this.generator.generate(ImportedServiceFactory.this.service, method), serial)));
		}
	}
}
