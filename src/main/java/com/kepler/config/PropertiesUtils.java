package com.kepler.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author kim 2015年7月16日
 */
/**
 * @author kim
 *
 * 2016年2月13日
 */
/**
 * @author kim
 *
 * 2016年2月13日
 */
public class PropertiesUtils {

	/**
	 * 动态配置文件物理路径(优先级2)
	 */
	public static final String FILE_DYNAMIC = System.getProperty("dynamic", "classpath:kepler.dynamic");

	/**
	 * Version文件物理路径(优先级1)
	 */
	public static final String FILE_VERSION = System.getProperty("version", "classpath:kepler.version");

	/**
	 * 框架配置文件物理路径(优先级0)
	 */
	public static final String FILE_CONFIG = System.getProperty("conf", "classpath:kepler.conf");

	/**
	 * 是否在新配置生效前对当前配置备份
	 */
	private static final boolean BACKUP = Boolean.valueOf(System.getProperty("backup", "true"));

	/**
	 * 当前配置数据(内存快照)
	 */
	private static final Map<String, String> PROPERTIES = new HashMap<String, String>();

	private static final Log LOGGER = LogFactory.getLog(PropertiesUtils.class);

	static {
		PropertiesUtils.init(PropertiesUtils.PROPERTIES);
	}

	/**
	 * 从配置文件读取数据至指定Map, Config(0) -> Version(1) -> Dynamic(2). 高优先配置覆盖低优先配置.
	 * 
	 * @param to
	 * @return
	 */
	private static Map<String, String> init(Map<String, String> to) {
		Properties properties = new Properties();
		PropertiesUtils.LOGGER.info("Loading config file: " + PropertiesUtils.FILE_CONFIG);
		PropertiesUtils.loading(PropertiesUtils.FILE_CONFIG, properties);
		PropertiesUtils.LOGGER.info("Loading version file: " + PropertiesUtils.FILE_VERSION);
		PropertiesUtils.loading(PropertiesUtils.FILE_VERSION, properties);
		PropertiesUtils.LOGGER.info("Loading dynamic file: " + PropertiesUtils.FILE_DYNAMIC);
		PropertiesUtils.loading(PropertiesUtils.FILE_DYNAMIC, properties);
		PropertiesUtils.convert(to, properties);
		return to;
	}

	/**
	 * Hashtable转换为HashMap
	 * 
	 * @param from
	 */
	private static void convert(Map<String, String> to, Properties from) {
		for (Object each : from.keySet()) {
			String key = each.toString();
			String value = from.getProperty(key);
			// Warning: trim
			to.put(key.trim().toLowerCase(), value.trim());
			PropertiesUtils.LOGGER.debug("Loading property key=" + key + " ,value=" + value);
		}
	}

	/**
	 * 加载指定配置至Properties
	 * 
	 * @param file
	 * @param properties
	 */
	private static void loading(String file, Properties properties) {
		try (InputStream input = ResourceUtils.getURL(file).openStream()) {
			properties.load(input);
		} catch (Throwable throwable) {
			PropertiesUtils.LOGGER.warn(throwable.getMessage(), throwable);
		}
	}

	/**
	 * 将Config中剩余配置合并至Properties
	 * 
	 * @param config
	 * @param current
	 */
	private static void merge2all(Map<String, String> config, Properties current) {
		for (String key : config.keySet()) {
			current.put(key, config.get(key));
			PropertiesUtils.LOGGER.debug("Merge2all: key=" + key + " ,value=" + config.get(key));
		}
	}

	/**
	 * 将Config中交集配置合并至Properties
	 * 
	 * @param config
	 * @param current
	 */
	private static void merge2intersection(Map<String, String> config, Properties current) {
		for (Object key : current.keySet()) {
			if (config.get(key) != null) {
				// Remove
				current.put(key, config.remove(key));
				PropertiesUtils.LOGGER.debug("Merge2intersection: key=" + key + " ,value=" + current.get(key));
			}
		}
	}

	private static void merge(Map<String, String> config, Properties properties, boolean intersection) {
		if (intersection) {
			PropertiesUtils.merge2all(config, properties);
		} else {
			PropertiesUtils.merge2intersection(config, properties);
		}
	}

	/**
	 * 备份历史配置
	 * 
	 * @param files
	 */
	private static void backup(String... files) {
		if (PropertiesUtils.BACKUP) {
			// 备份后缀
			long suffix = System.currentTimeMillis();
			for (String current : files) {
				try {
					Path source = Paths.get(ResourceUtils.getURL(current).getFile());
					Path target = Paths.get(ResourceUtils.getURL(current).getFile() + "." + suffix);
					Files.copy(source, target, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
				} catch (Throwable throwable) {
					PropertiesUtils.LOGGER.debug(throwable.getMessage(), throwable);
				}
			}
		}
	}

	private static void store(Map<String, String> config, String... files) {
		for (int index = 0; index < files.length; index++) {
			String current = files[index];
			// 排序Properties
			Properties properties = new OrderedProperties();
			// 从磁盘加载历史配置
			PropertiesUtils.loading(current, properties);
			// 历史配置合并当前配置, 最后的文件(index == files.length - 1)采用MergeAll
			PropertiesUtils.merge(config, properties, index == files.length - 1);
			try (OutputStream output = new FileOutputStream(new File(ResourceUtils.getURL(current).getFile()))) {
				// 物理存储
				properties.store(output, null);
				PropertiesUtils.LOGGER.warn("Restore properties file: " + current);
			} catch (Throwable throwable) {
				PropertiesUtils.LOGGER.debug(throwable.getMessage(), throwable);
			}
		}
	}

	public static String get(String key) {
		return PropertiesUtils.get(key, null);
	}

	/**
	 * 1, System.getProperty -> 2, 内存快照 -> 3, 默认值
	 * 
	 * @param key
	 * @param def
	 * @return
	 */
	public static String get(String key, String def) {
		return System.getProperty(key, StringUtils.defaultString(PropertiesUtils.PROPERTIES.get(key), def));
	}

	/**
	 * 获取Profile相关配置
	 * 
	 * @param profile
	 * @param key
	 * @return
	 */
	public static String profile(String profile, String key) {
		return PropertiesUtils.profile(profile, key, null);
	}

	public static String profile(String profile, String key, String def) {
		return PropertiesUtils.get(StringUtils.isEmpty(profile) ? key : profile + "." + key, PropertiesUtils.get(key, def));
	}

	public static int get(String key, int def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Integer.valueOf(value) : def;
	}

	public static byte profile(String key, String profile, byte def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Byte.valueOf(value) : def;
	}

	public static byte get(String key, byte def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Byte.valueOf(value) : def;
	}

	public static int profile(String key, String profile, int def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Integer.valueOf(value) : def;
	}

	public static short get(String key, short def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Short.valueOf(value) : def;
	}

	public static short profile(String key, String profile, short def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Short.valueOf(value) : def;
	}

	public static long get(String key, long def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Long.valueOf(value) : def;
	}

	public static long profile(String key, String profile, long def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Long.valueOf(value) : def;
	}

	public static float get(String key, float def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Float.valueOf(value) : def;
	}

	public static float profile(String key, String profile, float def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Float.valueOf(value) : def;
	}

	public static double get(String key, double def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Double.valueOf(value) : def;
	}

	public static double profile(String key, String profile, double def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Double.valueOf(value) : def;
	}

	public static boolean get(String key, boolean def) {
		String value = PropertiesUtils.get(key);
		return value != null ? Boolean.valueOf(value) : def;
	}

	public static boolean profile(String key, String profile, boolean def) {
		String value = PropertiesUtils.profile(key, profile);
		return value != null ? Boolean.valueOf(value) : def;
	}

	/**
	 * 获取当前配置快照
	 * @return
	 */
	public static Map<String, String> memory() {
		TreeMap<String, String> memory = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		memory.putAll(PropertiesUtils.PROPERTIES);
		return memory;
	}

	/**
	 * 获取当前配置(此方法将重新加载配置文件)
	 * @return
	 */
	public static Map<String, String> properties() {
		return PropertiesUtils.init(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER));
	}

	/**
	 * 更新配置
	 * 
	 * @param properties
	 */
	public static void properties(Map<String, String> properties) {
		// 同步内存(非事务, 不回滚)
		PropertiesUtils.PROPERTIES.putAll(properties);
		PropertiesUtils.backup(PropertiesUtils.FILE_CONFIG, PropertiesUtils.FILE_VERSION, PropertiesUtils.FILE_DYNAMIC);
		PropertiesUtils.store(properties, PropertiesUtils.FILE_CONFIG, PropertiesUtils.FILE_VERSION, PropertiesUtils.FILE_DYNAMIC);
	}

	/**
	 * 有序Properties
	 * 
	 * @author kim 2016年1月11日
	 */
	private static class OrderedProperties extends Properties {

		private static final long serialVersionUID = -1;

		private final Set<String> delegate = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

		public Object put(Object key, Object value) {
			this.delegate.add(key.toString());
			return super.put(key, value);
		}

		@SuppressWarnings("unchecked")
		public Enumeration<Object> keys() {
			return Collections.<Object> enumeration((Set<Object>) (Set<?>) this.delegate);
		}

		@SuppressWarnings("unchecked")
		public Set<Object> keySet() {
			return (Set<Object>) (Set<?>) this.delegate;
		}

		public Set<String> stringPropertyNames() {
			return this.delegate;
		}
	}
}
