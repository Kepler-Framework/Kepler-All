package com.kepler.generic.reflect.analyse.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.kepler.annotation.Generic;
import com.kepler.annotation.GenericElement;
import com.kepler.annotation.GenericParam;
import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.analyse.Fields;
import com.kepler.generic.reflect.analyse.FieldsAnalyser;
import com.kepler.generic.reflect.analyse.FieldsFilter;
import com.kepler.generic.reflect.convert.Convertor;
import com.kepler.generic.reflect.convert.ConvertorSelector;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.org.apache.commons.lang.reflect.MethodUtils;
import com.kepler.service.Exported;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultAnalyser implements Exported, FieldsAnalyser {

	/**
	 * 是否自动分析全部接口
	 */
	private static final boolean AUTOMATIC = PropertiesUtils.get(DefaultAnalyser.class.getName().toLowerCase() + ".automatic", false);

	/**
	 * 是否开启泛化
	 */
	private static final boolean ENABLED = PropertiesUtils.get(DefaultAnalyser.class.getName().toLowerCase() + ".enabled", true);

	/*可复用Extension*/
	private static final ThreadLocal<Extension> EXTENSION = new ThreadLocal<Extension>() {
		protected Extension initialValue() {
			return new Extension();
		}
	};

	private static final Log LOGGER = LogFactory.getLog(DefaultAnalyser.class);

	/**
	 * 默认扩展(空)
	 */
	public static final Class<?>[] EMPTY = new Class<?>[] {};

	/**
	 * Extension - Fields映射
	 */
	private final Map<Extension, Fields> fields = new HashMap<Extension, Fields>();

	/**
	 * Method - Fields[]映射
	 */
	private final Map<Method, Fields[]> methods = new HashMap<Method, Fields[]>();

	private final ConvertorSelector selector;

	private final FieldsFilter filter;

	public DefaultAnalyser(ConvertorSelector selector, FieldsFilter filter) {
		super(); 
		this.selector = selector;
		this.filter = filter;
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		// 处理接口标记Generic的方法
		for (Method method_interface : Arrays.asList(Class.forName(service.service()).getMethods())) {
			// 获取对应实现类的Method
			Method method_service = MethodUtils.getAccessibleMethod(instance.getClass(), method_interface.getName(), method_interface.getParameterTypes());
			// 开启泛化, 并且开启自动分析或标记Generic则分析属性
			if (DefaultAnalyser.ENABLED && (DefaultAnalyser.AUTOMATIC || AnnotationUtils.findAnnotation(method_service, Generic.class) != null)) {
				Fields[] fields = new Fields[method_service.getParameterTypes().length];
				for (int index = 0; index < method_service.getParameterTypes().length; index++) {
					List<Class<?>> annotation_param = this.extension4param(method_service.getParameterAnnotations()[index]);
					// 分析参数, 并传递扩展信息(优先采用Annotation)
					fields[index] = this.set(method_service.getParameterTypes()[index], !annotation_param.isEmpty() ? annotation_param.toArray(new Class<?>[] {}) : this.extension(method_service.getParameterTypes()[index], method_service.getGenericParameterTypes()[index]));
				}
				// 放入Method缓存
				this.methods.put(method_service, fields);
			}
		}
	}

	/**
	 * 遍历参数Annotation获取Generic属性
	 * 
	 * @param method_service
	 * @param index
	 * @return
	 */
	private List<Class<?>> extension4param(Annotation[] annotations) {
		List<Class<?>> extensions = new ArrayList<Class<?>>();
		for (Annotation each : annotations) {
			// For Spring, 必须使用IsAssignableFrom
			if (GenericParam.class.isAssignableFrom(each.getClass())) {
				for (Class<?> clazz : GenericParam.class.cast(each).value()) {
					extensions.add(clazz);
				}
			}
		}
		return extensions;
	}

	@Override
	public Fields set(Class<?> clazz) {
		return this.set(clazz, DefaultAnalyser.EMPTY);
	}

	@Override
	public Fields set(Class<?> clazz, Class<?>[] extension) {
		// 递归, 尝试分析扩展
		for (Class<?> each : extension) {
			this.set(each);
		}
		// 构建Key
		Extension actual = new Extension(clazz, extension);
		// Guard case, 已存在则获取后返回
		if (this.fields.containsKey(actual)) {
			return this.fields.get(actual);
		}
		// 分析ObjectFields (符合Object判定条件)
		if (!this.filter.filter(actual.clazz())) {
			ObjectFields fields = new ObjectFields(actual.clazz(), extension);
			// 先入缓存后分析(ObjectFields分析过程会产生递归分析)
			this.fields.put(actual, fields);
			DefaultAnalyser.LOGGER.info("Analyse fields[object]: " + actual);
			return fields.fields();
		} else {
			// 分析DefaultFields
			Fields fields = new DefaultFields(this.selector.select(clazz), actual);
			this.fields.put(actual, fields);
			DefaultAnalyser.LOGGER.info("Analyse fields[default]: " + actual);
			return fields;
		}
	}

	public Fields get(Class<?> clazz) {
		return this.get(clazz, DefaultAnalyser.EMPTY);
	}

	public Fields get(Class<?> clazz, Class<?>[] extension) {
		return this.fields.get(DefaultAnalyser.EXTENSION.get().reset(clazz, extension));
	}

	@Override
	public Fields[] get(Method method) {
		return this.methods.get(method);
	}

	/**
	 * 属性值是否兼容
	 * 
	 * @param value
	 * @return
	 */
	private boolean assignable(Object value, Class<?> clazz) {
		// Guard case
		if (value == null) {
			return true;
		}
		// 属性兼容并且属性不为Collection, Map或数组(需要额外判断)
		return clazz.isAssignableFrom(value.getClass()) && !Collection.class.isAssignableFrom(value.getClass()) && !Map.class.isAssignableFrom(value.getClass()) && !clazz.isArray();
	}

	/**
	 * 获取参数扩展
	 * 
	 * @param clazz 参数类型
	 * @param generic 参数泛化
	 * @return
	 */
	private Class<?>[] extension(Class<?> clazz, Type generic) {
		// Guard case1, 数组
		if (clazz.isArray()) {
			return new Class<?>[] { clazz.getComponentType() };
		}
		// Guard case2, 集合/Map
		if (ParameterizedType.class.isAssignableFrom(generic.getClass())) {
			List<Class<?>> extension = new ArrayList<Class<?>>();
			ParameterizedType param = ParameterizedType.class.cast(generic);
			for (Type each : param.getActualTypeArguments()) {
				extension.add(Class.class.cast(each));
			}
			return extension.toArray(new Class[] {});
		}
		// 不为数组也不为集合/Map使用空扩展
		return DefaultAnalyser.EMPTY;
	}

	/**
	 * 类型及扩展
	 * 
	 * @author KimShen
	 *
	 */
	private static class Extension {

		private Class<?>[] extension;

		private Class<?> clazz;

		private Extension() {
		}

		/**
		 * 常规构造
		 * 
		 * @param clazz
		 * @param extension
		 */
		private Extension(Class<?> clazz, Class<?>[] extension) {
			this.extension = extension;
			this.clazz = clazz;
		}

		/**
		 * 复用重置
		 * 
		 * @param clazz
		 * @param extension
		 * @return
		 */
		private Extension reset(Class<?> clazz, Class<?>[] extension) {
			this.extension = extension;
			this.clazz = clazz;
			return this;
		}

		public Class<?>[] extension() {
			return this.extension;
		}

		public Class<?> clazz() {
			return this.clazz;
		}

		public int hashCode() {
			int hash = 0;
			hash = hash ^ this.clazz.hashCode();
			for (Class<?> each : this.extension) {
				hash = hash ^ each.hashCode();
			}
			return hash;
		}

		public boolean equals(Object ob) {
			Extension target = Extension.class.cast(ob);
			// Guard case1, 类型不一致
			if (!this.clazz.equals(target.clazz)) {
				return false;
			}
			// Guard case2, 扩展长度不相等
			if (this.extension.length != target.extension.length) {
				return false;
			}
			// Guard case3, 扩展类型不相等
			for (int index = 0; index < this.extension.length; index++) {
				if (!this.extension[index].equals(target.extension[index])) {
					return false;
				}
			}
			return true;
		}

		public String toString() {
			return "[class=" + this.clazz + "][extension=" + Arrays.toString(this.extension) + "]";
		}
	}

	/**
	 * 自定义对象Fields
	 * 
	 * @author KimShen
	 *
	 */
	private class ObjectFields implements Fields {

		private static final String PREFIX_SET = "set";

		private static final String PREFIX_GET = "get";

		/**
		 * 与Class绑定的Set操作
		 */
		private final List<ObjectFieldSetter> setters = new ArrayList<ObjectFieldSetter>();

		private final Class<?>[] extension;

		private final Class<?> clazz;

		/**
		 * @param clazz 目标Class
		 * @param extension 扩展信息
		 * @param analyser 分析器,用于回调
		 * @param selector 转换器
		 */
		private ObjectFields(Class<?> clazz, Class<?>[] extension) {
			super();
			this.extension = extension;
			this.clazz = clazz;
		}

		/**
		 * 初始化属性集合
		 * 
		 * @return
		 */
		public ObjectFields fields() {
			// 分析所有set方法
			for (Method method : this.clazz.getMethods()) {
				if (this.allowed(method)) {
					// Generic from Set/Get
					GenericElement element = this.annotation(this.clazz, method);
					// 获取扩展信息(优先从Generic)
					Class<?>[] extensions = element != null ? element.value() : DefaultAnalyser.this.extension(method.getParameterTypes()[0], method.getGenericParameterTypes()[0]);
					// 方法缩写
					String abbr = method.getName().replaceFirst(ObjectFields.PREFIX_SET, "");
					// 追加setter
					this.setters.add(new ObjectFieldSetter(String.valueOf(abbr.toCharArray()[0]).toLowerCase() + abbr.substring(1, abbr.length()), method, extensions, DefaultAnalyser.this.selector.select(method.getParameterTypes()[0])));
					// 尝试递归分析方法参数及对应扩展
					DefaultAnalyser.this.set(method.getParameterTypes()[0], extensions);
				}
			}
			return this;
		}

		/**
		 * 指定方法是否需要解析
		 * 
		 * @param method
		 * @return
		 */
		private boolean allowed(Method method) {
			// public void set(x)
			boolean allowed = method.getName().startsWith(ObjectFields.PREFIX_SET) && method.getReturnType().equals(void.class) && method.getParameterTypes().length == 1;
			if (!allowed) {
				DefaultAnalyser.LOGGER.debug("Method: " + method + " will be filtered");
			}
			return allowed;
		}

		/**
		 * 从Set/Get获取Generic注释
		 * 
		 * @param clazz
		 * @param method Set方法
		 * @return
		 */
		private GenericElement annotation(Class<?> clazz, Method method) {
			GenericElement element = AnnotationUtils.findAnnotation(method, GenericElement.class);
			if (element != null) {
				return element;
			}
			// 尝试获取getter
			Method getter = MethodUtils.getAccessibleMethod(clazz, method.getName().replaceFirst(ObjectFields.PREFIX_SET, ObjectFields.PREFIX_GET), new Class<?>[] {});
			return getter != null ? AnnotationUtils.findAnnotation(getter, GenericElement.class) : null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object actual(Object source) throws Exception {
			// Object数据源必须为Map
			Map<String, Object> source_actual = Map.class.cast(source);
			// 如果存在扩展则使用扩展创建对象
			Object bean = this.extension.length != 0 ? this.extension[0].newInstance() : this.clazz.newInstance();
			// 轮询所有Setter并进行赋值
			for (ObjectFieldSetter setter : this.setters) {
				setter.invoke(bean, source_actual.get(setter.abbr()));
			}
			return bean;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		/**
		 * 属性写入
		 * 
		 * @author KimShen
		 *
		 */
		private class ObjectFieldSetter {

			/**
			 * 扩展属性
			 */
			private final Class<?>[] extension;

			/**
			 * 转换器
			 */
			private final Convertor convertor;

			/**
			 * 实际类
			 */
			private final Class<?> clazz;

			/**
			 * Set方法
			 */
			private final String setter;

			/**
			 * Set方法缩写
			 */
			private final String abbr;

			/**
			 * @param abbr
			 * @param setter 
			 * @param convertor 转换器
			 * @param extension 扩展信息
			 */
			private ObjectFieldSetter(String abbr, Method setter, Class<?>[] extension, Convertor convertor) {
				super();
				this.abbr = abbr;
				this.extension = extension;
				this.convertor = convertor;
				this.setter = setter.getName();
				this.clazz = setter.getParameterTypes()[0];
			}

			/**
			 * 获取方法缩写
			 * 
			 * @return
			 */
			public String abbr() {
				return this.abbr;
			}

			/**
			 * 属性实际写入
			 * 
			 * @param ob 目标Bean
			 * @param value 目标值
			 * @throws Exception
			 */
			public void invoke(Object ob, Object value) throws Exception {
				// 转换后写入
				MethodUtils.invokeMethod(ob, this.setter, DefaultAnalyser.this.assignable(value, this.clazz) ? value : this.convertor.convert(value, this.clazz, this.extension, DefaultAnalyser.this));
			}

			public String toString() {
				return ToStringBuilder.reflectionToString(this);
			}
		}
	}

	/**
	 * 非自定义对象Fields, List, Set, Map
	 * 
	 * @author KimShen
	 *
	 */
	private class DefaultFields implements Fields {

		private final Convertor convertor;

		private final Extension extension;

		public DefaultFields(Convertor convertor, Extension extension) {
			super();
			this.extension = extension;
			this.convertor = convertor;
		}

		@Override
		public Object actual(Object source) throws Exception {
			return DefaultAnalyser.this.assignable(source, this.extension.clazz()) ? source : this.convertor.convert(source, this.extension.clazz(), this.extension.extension(), DefaultAnalyser.this);
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
