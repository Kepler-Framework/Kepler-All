package com.kepler.main.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.kepler.KeplerLocalException;
import com.kepler.KeplerValidateException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.impl.DefaultServer;
import com.kepler.main.Pid;

/**
 * @author kim 2015年7月15日
 */
public class StartPID implements Pid, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

	/**
	 * PID文件前缀
	 */
	private static final String PREFIX = PropertiesUtils.get(StartPID.class.getName().toLowerCase() + ".file", "kepler") + "_";

	/**
	 * 是否禁止相同目录多次启动
	 */
	private static final boolean CONFLICT = PropertiesUtils.get(StartPID.class.getName().toLowerCase() + ".conflict", false);

	/**
	 * 是否强制生成PID文件(如Client)
	 */
	private static final boolean FORCE = PropertiesUtils.get(StartPID.class.getName().toLowerCase() + ".force", false);

	private static final Log LOGGER = LogFactory.getLog(StartPID.class);

	private final String pid;

	private final File file;

	public StartPID() throws Exception {
		super();
		// 获取PID并创建文件
		this.pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		this.file = this.conflict(new File(StartPID.PREFIX + this.pid + ".pid"));
		this.file.deleteOnExit();
	}

	private File conflict(File pid) {
		StartPID.LOGGER.info("PID path: " + pid.getAbsolutePath());
		// 是否已存在启动的进程
		String[] pids = pid.getAbsoluteFile().getParentFile().list();
		if (pids != null) {
			for (String each : pids) {
				if (StartPID.CONFLICT && each.matches("^" + StartPID.PREFIX + "\\d.*")) {
					throw new KeplerValidateException("Kepler was started on " + each + " ... ");
				}
			}
		}
		return pid;
	}

	private void create() {
		try (Writer writer = new FileWriter(this.file)) {
			writer.write(this.pid);
		} catch (IOException e) {
			throw new KeplerLocalException(e);
		}
	}

	public String pid() {
		return this.pid;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// Create only exists DefaultServer
		if (DefaultServer.class.isAssignableFrom(bean.getClass())) {
			this.create();
		}
		return bean;
	}

	// Double check, 如果指定Force则Client也将创建PID File
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (StartPID.FORCE && !this.file.exists()) {
			this.create();
		}
	}
}
