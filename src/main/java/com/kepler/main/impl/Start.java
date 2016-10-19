package com.kepler.main.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kepler.config.PropertiesUtils;
import com.kepler.main.Demotion;
import com.kepler.main.Prepare;

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
	 */
	private static String[] configs() {
		List<String> configs = new ArrayList<String>();
		// 默认启动XM
		configs.add("classpath:" + System.getProperty("kepler.main", "kepler-runtime.xml"));
		// 加载插件
		for (String plugin : System.getProperty("kepler.plugin", "").split(";")) {
			configs.add("classpath:" + plugin);
		}
		return configs.toArray(new String[] {});
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
