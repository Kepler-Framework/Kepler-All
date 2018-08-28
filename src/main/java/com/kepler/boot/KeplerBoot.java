package com.kepler.boot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.kepler.config.PropertiesUtils;

/**
 * @author Sulong
 *
 */
public class KeplerBoot implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	public static final KeplerBoot INSTANCE = new KeplerBoot();

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		Iterator<PropertySource<?>> sources = context.getEnvironment().getPropertySources().iterator();
		List<PropertySource<?>> copies = new ArrayList<PropertySource<?>>();
		while (sources.hasNext()) {
			copies.add(sources.next());
		}
		Map<String, String> properties = new HashMap<String, String>();
		/**
		 * 倒序，以便于让列表中靠前的PropertySource覆盖之后的PropertySource中的同名的属性。
		 */
		for (int i = copies.size() - 1; i >= 0; i--) {
			PropertySource<?> source = copies.get(i);
			/**
			 * 不是EnumerablePropertySource类型的, 没有办法预先知道属性名所有没有办法取出来
			 */
			if (!(source instanceof EnumerablePropertySource)) {
				continue;
			}
			EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
			if (enumerable.getPropertyNames() == null) {
				continue;
			}
			for (String property : enumerable.getPropertyNames()) {
				if (!enumerable.containsProperty(property)) {
					continue;
				}
				properties.put(property, String.valueOf(enumerable.getProperty(property)));
			}
		}
		PropertiesUtils.properties(properties);
	}
}
