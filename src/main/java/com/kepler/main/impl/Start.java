package com.kepler.main.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kepler.config.PropertiesUtils;
import com.kepler.main.Demotion;
import com.kepler.main.Prepare;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author kim 2015年7月13日
 */
public class Start {

	private static final Log LOGGER = LogFactory.getLog(Start.class);

	public static void main(String[] args) throws Exception {
		try {
			// 加载Prepare
			Start.prepare();
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(Start.configs());
			Runtime.getRuntime().addShutdownHook(new Shutdown(context));
			Start.wait(context);
			Start.LOGGER.warn("Service closed ...");
		} catch (Throwable e) {
			e.printStackTrace();
			Start.LOGGER.fatal(e.getMessage(), e);
			System.exit(1);
		}
	}

	/**
	 * 读取配置文件
	 * 
	 * @return
	 * @throws IOException 
	 */
	private static String[] configs() {
		List<String> configs = new ArrayList<String>();
		// 默认启动XML
		Start.loading4default(configs);
		// 加载指定插件
		Start.loading4config(configs);
		// 加载默认插件
		Start.loading4path(configs);
		Start.LOGGER.warn("Loading configs: " + configs);
		return configs.toArray(new String[] {});
	}

	/**
	 * 加载核心配置
	 * 
	 * @param configs
	 */
	private static void loading4default(List<String> configs) {
		configs.add("classpath:" + System.getProperty("kepler.main", "kepler-runtime.xml"));
	}

	/**
	 * 加载指定插件
	 * 
	 * @param configs
	 */
	private static void loading4config(List<String> configs) {
		for (String plugin : System.getProperty("kepler.plugin.list", "").split(";")) {
			if (StringUtils.isNotEmpty(plugin)) {
				configs.add("classpath:" + plugin);
			}
		}
	}

	/**
	 * 加载默认插件 @author zhangjiehao
	 * 
	 * @param configs
	 */
	private static void loading4path(List<String> configs) {
		String prefix = System.getProperty("kepler.plugin.prefix", "kepler-plugin-");
		String suffix = System.getProperty("kepler.plugin.suffix", ".xml");
		for (URL url : URLClassLoader.class.cast(Start.class.getClassLoader()).getURLs()) {
			File file = new File(url.getFile());
			// 如果为目录则扫描目录(仅一层, 如WEB-INF)
			if (file.isDirectory()) {
				for (File entry : file.listFiles()) {
					if (entry.getName().startsWith(prefix) && entry.getName().endsWith(suffix)) {
						configs.add("classpath:" + entry.getName());
					}
				}
			} else if (file.getName().endsWith(".jar")) {
				// 如果为Jar包则扫描
				try (JarFile jar = new JarFile(file)) {
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.getName().startsWith(prefix) && entry.getName().endsWith(suffix)) {
							configs.add("classpath:" + entry.getName());
						}
					}
				} catch (IOException e) {
					Start.LOGGER.error("Failed decompressing the JAR");
				}
			}
		}
	}

	/**
	 * 前置初始化
	 *
	 * @throws Exception
	 */
	private static void prepare() throws Exception {
		if (Prepare.CLASS != null) {
			for (String clazz : Prepare.CLASS.split(";")) {
				Prepare.class.cast(Class.forName(clazz).newInstance()).prepare();
			}
		}
	}

	/**
	 * Hold主线程
	 *
	 * @param context
	 *
	 * @throws InterruptedException
	 */
	private static void wait(ClassPathXmlApplicationContext context) throws InterruptedException {
		synchronized (context) {
			while (context.isActive()) {
				context.wait();
			}
		}
	}

	/**
	 * Shutdowo hook
	 *
	 * @author kim 2016年1月11日
	 */
	private static class Shutdown extends Thread {

		private final ClassPathXmlApplicationContext context;

		private Shutdown(ClassPathXmlApplicationContext context) {
			super();
			this.context = context;
		}

		@Override
		public void run() {
			synchronized (this.context) {
				// 服务降级
				this.demote();
				// 先关闭后唤醒
				this.context.close();
				this.context.notifyAll();
			}
		}

		private void demote() {
			// 等待间隔
			int waiting = PropertiesUtils.get(Start.class.getName().toLowerCase() + ".demote_waiting", 500);
			try {
				for (Demotion each : this.context.getBeansOfType(Demotion.class).values()) {
					each.demote();
				}
				Thread.sleep(waiting);
			} catch (Exception e) {
				Start.LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
