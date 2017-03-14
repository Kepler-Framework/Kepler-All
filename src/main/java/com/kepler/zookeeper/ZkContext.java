package com.kepler.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.kepler.KeplerLocalException;
import com.kepler.admin.status.Status;
import com.kepler.annotation.Internal;
import com.kepler.config.Config;
import com.kepler.config.Profile;
import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.host.HostStatus;
import com.kepler.host.impl.DefaultHostStatus;
import com.kepler.host.impl.ServerHost;
import com.kepler.host.impl.ServerHost.Builder;
import com.kepler.main.Demotion;
import com.kepler.serial.Serials;
import com.kepler.service.Exported;
import com.kepler.service.Imported;
import com.kepler.service.ImportedListener;
import com.kepler.service.Service;
import com.kepler.service.ServiceInstance;
import com.kepler.service.imported.ImportedService;

/**
 * @author zhangjiehao 2015年7月9日
 */
/**
 * @author KimShen
 *
 */
public class ZkContext implements Demotion, Imported, Exported, Runnable, ApplicationListener<ContextRefreshedEvent> {

	/**
	 * 保存依赖关系路径
	 */
	public static final String DEPENDENCY = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".dependency", "_dependency");

	/**
	 * 保存配置信息路径
	 */
	public static final String CONFIG = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".config", "_configs");

	/**
	 * 保存配置信息路径, 如果失败是否抛出异常终止发布
	 */
	public static final boolean CONFIG_FROCE = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".config_force", false);

	/**
	 * 保存状态信息路径
	 */
	public static final String STATUS = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".status", "_status");

	/**
	 * 保存状态信息路径, 如果失败是否抛出异常终止发布
	 */
	public static final boolean STATUS_FROCE = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".status_force", false);

	private static final int INTERVAL = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".interval", 60000);

	private static final int DELAY = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".delay", 30000);

	/**
	 * 保存服务信息路径
	 */
	public static final String ROOT = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".root", "/kepler");

	/**
	 * 是否发布依赖
	 */
	public static final String DEPENDENCY_KEY = ZkContext.class.getName().toLowerCase() + ".dependency";

	public static final boolean DEPENDENCY_VAL = PropertiesUtils.get(ZkContext.DEPENDENCY_KEY, true);

	/**
	 * 是否发布
	 */
	public static final String EXPORT_KEY = ZkContext.class.getName().toLowerCase() + ".export";

	public static final boolean EXPORT_VAL = PropertiesUtils.get(ZkContext.EXPORT_KEY, true);

	/**
	 * 是否导入
	 */
	public static final String IMPORT_KEY = ZkContext.class.getName().toLowerCase() + ".import";

	public static final boolean IMPORT_VAL = PropertiesUtils.get(ZkContext.IMPORT_KEY, true);

	public static final long REFRESH_INTERVAL = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + "" + ".refresh_interval", 60 * 1000);

	private static final Log LOGGER = LogFactory.getLog(ZkContext.class);

	/**
	 * 加载失败的服务
	 */
	private final BlockingQueue<DelayInstall> uninstalled = new DelayQueue<DelayInstall>();

	private final ZkWatcher watcher = new ZkWatcher();

	private final Snapshot snapshot = new Snapshot();

	private final Exports exports = new Exports();

	private final Roadmap road = new Roadmap();

	/**
	 * 用于延迟发布
	 */
	private final Delay delay = new Delay();

	private final ThreadPoolExecutor threads;

	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();;

	private final RefreshRunnable refreshRunnable = new RefreshRunnable();

	private final ImportedListener listener;

	private final ServerHost local;

	private final Profile profile;

	private final Serials serials;

	private final Status status;

	private final Config config;

	private final ZkClient zoo;

	volatile private boolean shutdown;

	public ZkContext(ThreadPoolExecutor threads, ImportedListener listener, ServerHost local, Serials serials, Profile profile, Config config, Status status, ZkClient zoo) {
		super();
		this.zoo = zoo.bind(this);
		this.listener = listener;
		this.profile = profile;
		this.serials = serials;
		this.threads = threads;
		this.config = config;
		this.status = status;
		this.local = local;
	}

	/**
	 * For Spring
	 */
	public void init() {
		// 单线程操作
		this.threads.execute(this);
		this.scheduledExecutorService.scheduleAtFixedRate(this.refreshRunnable, REFRESH_INTERVAL, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		this.shutdown = true;
		// 注销已发布服务
		this.exports.destroy();
		// 关闭ZK
		this.zoo.close();
		this.scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * 重新导入服务
	 * 
	 * @throws Exception
	 */
	private void reset4imported() throws Exception {
		for (Service service : this.snapshot.imported) {
			// 获取所有快照依赖并重新导入
			this.subscribe(service);
		}
		ZkContext.LOGGER.info("Reset imported success ...");
	}

	/**
	 * 重新发布服务
	 * 
	 * @throws Exception
	 */
	private void reset4exported() throws Exception {
		// 从快照获取需发布服务
		for (Service service : this.snapshot.exported.keySet()) {
			// 获取所有服务实例并重新发布
			this.exported(service, this.snapshot.exported.get(service));
		}
		ZkContext.LOGGER.info("Reset exported success ...");
	}

	/**
	 * 卸载所有实例(清空本地Host)
	 * 
	 * @throws Exception
	 */
	private void reset4instance() throws Exception {
		for (ServiceInstance instance : this.snapshot.instances.values()) {
			this.listener.delete(instance);
		}
		ZkContext.LOGGER.info("Reset instance success ...");
	}

	/**
	 * 发布Status节点
	 * 
	 * @throws Exception
	 */
	private void status() throws Exception {
		// 开启并尚未注册
		if (this.exports.status()) {
			try {
				this.exports.status(this.zoo.create(this.road.mkdir(new StringBuffer(ZkContext.ROOT).append(ZkContext.STATUS).toString()) + "/" + this.local.sid(), this.serials.def4output().output(new DefaultHostStatus(this.local, this.status.get()), HostStatus.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
			} catch (NodeExistsException exception) {
				// 如果强制发布Status则终止发布
				if (ZkContext.STATUS_FROCE) {
					throw exception;
				} else {
					ZkContext.LOGGER.warn("Status node can not create: " + this.local.sid());
				}
			}
		}
	}

	/**
	 * 发布Config节点(并监听)
	 * 
	 * @throws Exception
	 */
	private void config() throws Exception {
		if (this.exports.config()) {
			try {
				this.exports.config(new ConfigWatcher(this.zoo.create(this.road.mkdir(new StringBuffer(ZkContext.ROOT).append(ZkContext.CONFIG).toString()) + "/" + this.local.sid(), this.serials.def4output().output(PropertiesUtils.memory(), Map.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)).path());
			} catch (NodeExistsException exception) {
				// 如果强制发布Config则终止发布
				if (ZkContext.CONFIG_FROCE) {
					throw exception;
				} else {
					ZkContext.LOGGER.warn("Config node can not create: " + this.local.sid());
				}
			}
		}
	}

	/**
	 * 发布服务依赖
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void dependency(Service service) throws Exception {
		// 指定服务是否需要注册依赖
		if (PropertiesUtils.profile(this.profile.profile(service), ZkContext.DEPENDENCY_KEY, ZkContext.DEPENDENCY_VAL)) {
			this.zoo.create(this.road.mkdir(this.road.road(new StringBuffer(ZkContext.ROOT).append(ZkContext.DEPENDENCY).toString(), service.service(), service.versionAndCatalog())) + "/", this.serials.def4output().output(new ImportedService(this.local, service), ImportedService.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		}
	}

	public void demote() throws Exception {
		// 降级已发布服务
		this.exports.demote();
	}

	/**
	 * 重置/重连
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception {
		// 注销已发布服务
		this.exports.destroy();
		// 卸载所有实例, 重新发布服务, 重新加载实例, 重新发布Status节点, 重新发布Config节点
		this.reset4instance();
		this.reset4exported();
		this.reset4imported();
		this.status();
		this.config();
	}

	@Override
	public void subscribe(Service service) throws Exception {
		// 是否加载远程服务
		if (!PropertiesUtils.profile(this.profile.profile(service), ZkContext.IMPORT_KEY, ZkContext.IMPORT_VAL)) {
			ZkContext.LOGGER.warn("Disabled import service: " + service + " ... ");
			return;
		}
		// 订阅服务并启动Watcher监听

		refreshRunnable.isRunning = true;
		try {
			if (this.watcher.watch(service, this.road.road(ZkContext.ROOT, service.service(), service.versionAndCatalog()))) {
				// 加入本地快照
				this.snapshot.subscribe(service);
				// 发布服务依赖
				this.dependency(service);
				ZkContext.LOGGER.info("Import service: " + service);
			}
		} finally {
			refreshRunnable.isRunning = false;
		}
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		this.delay.exported(service, instance);
	}

	private void exported4delay(Service service, Object instance) throws Exception {
		// 是否发布远程服务
		if (!PropertiesUtils.profile(this.profile.profile(service), ZkContext.EXPORT_KEY, ZkContext.EXPORT_VAL)) {
			ZkContext.LOGGER.warn("Disabled export service: " + service + " ... ");
			return;
		}
		// 生成ZK节点(Profile Tag, Priority)
		ZkSerial serial = new ZkSerial(new Builder(this.local).setTag(PropertiesUtils.profile(this.profile.profile(service), Host.TAG_KEY, Host.TAG_VAL)).setPriority(Integer.valueOf(PropertiesUtils.profile(this.profile.profile(service), Host.PRIORITY_KEY, Host.PRIORITY_DEF))).toServerHost(), service);
		// 加入已导出服务列表
		this.exports.put(this.zoo.create(this.road.mkdir(this.road.road(ZkContext.ROOT, service.service(), service.versionAndCatalog())) + "/", this.serials.def4output().output(serial, ServiceInstance.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL), serial);
		// 加入已导出快照列表
		this.snapshot.exported(service, instance);
		ZkContext.LOGGER.info("Export service: " + service + " ... ");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			// 延迟发布
			this.delay.reach();
			// 启动完毕后发布Status/Config节点
			this.status();
			this.config();
		} catch (Throwable throwable) {
			throw new KeplerLocalException(throwable);
		}
	}

	@Override
	public void run() {
		while (!this.shutdown) {
			try {
				// 获取未加载服务并尝试重新加载
				DelayInstall service = this.uninstalled.poll(ZkContext.INTERVAL, TimeUnit.MILLISECONDS);
				if (service != null) {
					ZkContext.this.subscribe(service.service());
				}
			} catch (Throwable e) {
				ZkContext.LOGGER.debug(e.getMessage(), e);
			}
		}
		ZkContext.LOGGER.warn("ZkContext shutdown ... ");
	}

	private class Roadmap {

		/**
		 * 以 Content + "/"的形式追加路径
		 * @param buffer
		 * @param road
		 * @return
		 */
		private String road(StringBuffer buffer, String... road) {
			for (String each : road) {
				if (StringUtils.hasText(each)) {
					buffer.append(each).append("/");
				}
			}
			return buffer.substring(0, buffer.length() - 1);
		}

		/**
		 * 组合带前缀服务路径
		 * 
		 * @param service
		 * @param road
		 * @return
		 */
		public String road(String prefix, String service, String... road) {
			StringBuffer buffer = new StringBuffer(prefix).append("/").append(service).append("/");
			return this.road(buffer, road);
		}

		/**
		 * 递归创建路径
		 * 
		 * @param road
		 * @return
		 * @throws Exception
		 */
		public String mkdir(String road) throws Exception {
			StringBuffer buffer = new StringBuffer();
			for (String each : road.split("/")) {
				if (StringUtils.hasText(each)) {
					String current = buffer.append("/").append(each).toString();
					if (ZkContext.this.zoo.exists(current, true) == null) {
						ZkContext.this.zoo.create(current, new byte[] {}, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				}
			}
			return road;
		}
	}

	/**
	 * 已发布服务集合
	 * 
	 * @author KimShen
	 *
	 */
	private class Exports {

		/**
		 * 已发布服务(Path -> Instance)
		 */
		private final Map<String, ServiceInstance> exported = new ConcurrentHashMap<String, ServiceInstance>();

		/**
		 * 已发布Config路径
		 */
		private String config;

		/**
		 * 已发布Status路径
		 */
		private String status;

		/**
		 * 是否已发布Status
		 * 
		 * @return
		 */
		public boolean status() {
			return this.status == null;
		}

		/**
		 * 是否已发布Config
		 * 
		 * @return
		 */
		public boolean config() {
			return this.config == null;
		}

		/**
		 * 更新Status节点
		 * 
		 * @param status
		 */
		public void status(String status) {
			this.status = status;
		}

		/**
		 * 更新Config节点
		 * 
		 * @param config
		 */
		public void config(String config) {
			this.config = config;
		}

		/**
		 * 已发布服务
		 * 
		 * @param path
		 * @param instance
		 */
		public void put(String path, ServiceInstance instance) {
			this.exported.put(path, instance);
		}

		/**
		 * 服务降级, 将指定Path服务降级为优先级0
		 * 
		 * @param path
		 * @param instance
		 */
		private void demote(String path, ServiceInstance instance) {
			try {
				// 修改ZK节点数据
				ZkContext.this.zoo.setData(path, ZkContext.this.serials.def4output().output(new ZkSerial(new Builder(instance.host()).setPriority(0).toServerHost(), instance), ServiceInstance.class), -1);
				ZkContext.LOGGER.info("Demote service: " + instance.host());
			} catch (Exception e) {
				ZkContext.LOGGER.warn(e.getMessage(), e);
			}
		}

		/**
		 * 对所有已发布服务降级
		 * 
		 * @throws Exception
		 */
		public void demote() throws Exception {
			for (String path : this.exported.keySet()) {
				this.demote(path, this.exported.get(path));
			}
		}

		/**
		 * 注销指定ZK节点
		 * 
		 * @param path
		 */
		public void destroy(String path) {
			try {
				if (ZkContext.this.zoo.exists(path, false) != null) {
					ZkContext.this.zoo.delete(path, -1);
				}
				// 从已发布服务路径中移除
				this.exported.remove(path);
			} catch (Throwable e) {
				ZkContext.LOGGER.error(e.getMessage(), e);
			}
		}

		/**
		 * 注销Status节点
		 */
		public void destroy4status() {
			if (this.status != null) {
				// 删除ZK节点
				this.destroy(this.status);
				this.status = null;
			}
		}

		/**
		 * 注销Config节点
		 */
		public void destroy4config() {
			if (this.config != null) {
				// 删除ZK节点
				this.destroy(this.config);
				this.config = null;
			}
		}

		/**
		 * 注销已发布服务,已发布Status,已发布Config
		 */
		public void destroy() {
			for (String exported : this.exported.keySet()) {
				this.destroy(exported);
			}
			this.destroy4status();
			this.destroy4config();
		}
	}

	/**
	 * 快照
	 * 
	 * @author kim 2016年1月11日
	 */
	private class Snapshot implements Imported, Exported {

		/**
		 * 已导入实例(多线程竞争)
		 */
		private volatile Map<String, ServiceInstance> instances = new ConcurrentHashMap<String, ServiceInstance>();

		/**
		 * 已发布服务
		 */
		private final Map<Service, Object> exported = new HashMap<Service, Object>();

		/**
		 * 已导入服务
		 */
		private final Set<Service> imported = new HashSet<Service>();

		/**
		 * 获取并移除快照
		 * 
		 * @param path
		 * @return
		 */
		public ServiceInstance instance(String path) {
			return this.instances.remove(path);
		}

		public void instance(String path, ServiceInstance instance) {
			this.instances.put(path, instance);
		}

		@Override
		public void subscribe(Service service) {
			this.imported.add(service);
		}

		@Override
		public void exported(Service service, Object instance) {
			this.exported.put(service, instance);
		}

	}

	private class ZkWatcher {

		/**
		 * @param service
		 * @param path
		 * @return 是否初始化成功
		 * @throws Exception 
		 */
		public boolean watch(Service service, String path) throws Exception {
			try {
				// 获取所有Children Path, 并监听路径变化
				for (String child : new PathWatcher(path).snapshot()) {
					this.init(path, child);
				}
				return true;
			} catch (Throwable e) {
				// 如果为NoNodeException则进行重试
				if (e.getClass().equals(NoNodeException.class)) {
					this.failedIfInternal(service);
					// 尝试延迟加载
					ZkContext.this.uninstalled.add(new DelayInstall(service));
				} else {
					ZkContext.LOGGER.error(e.getMessage(), e);
				}
				return false;
			}
		}

		/**
		 * Internal 服务节点处理
		 * 
		 * @param service
		 */
		private void failedIfInternal(Service service) {
			try {
				// 标记为Internal的服务仅提示
				if (AnnotationUtils.findAnnotation(Class.forName(service.service()), Internal.class) != null) {
					ZkContext.LOGGER.info("Instances can not be found for internal service: " + service);
				} else {
					ZkContext.LOGGER.info("Instances can not be found for service: " + service);
				}
			} catch (ClassNotFoundException e) {
				// Generic
				ZkContext.LOGGER.info("Class not found: " + service);
			}
		}

		/**
		 * 初始化已注册服务, 并监听节点变化(节点首次加载)
		 * 
		 * @param path
		 * @param child
		 */
		private void init(String path, String child) {
			try {
				String actual = path + "/" + child;
				ServiceInstance instance = new DataWatcher(actual).snapshot();
				// 加载节点
				ZkContext.this.listener.add(instance);
				// 加载快照
				ZkContext.this.snapshot.instance(actual, instance);
			} catch (Throwable e) {
				ZkContext.LOGGER.info(e.getMessage(), e);
			}
		}
	}

	private class PathWatcher implements Watcher {

		private List<String> snapshot;

		private PathWatcher(String path) throws Exception {
			// 注册路径变化监听
			this.snapshot = ZkContext.this.zoo.getChildren(path, this);
			// 排序用于对比
			Collections.sort(this.snapshot);
		}

		/**
		 * 监听路径变化(节点新增)事件
		 * 
		 * @param event
		 */
		private void add(WatchedEvent event) {
			try {
				// 获取所有节点,对比新增节点
				List<String> previous = this.snapshot;
				this.snapshot = ZkContext.this.zoo.getChildren(event.getPath(), this);
				Collections.sort(this.snapshot);
				DiffContainer<String> container = new DiffContainer<String>(previous, this.snapshot);
				// 处理新增变化
				this.add(event.getPath(), container.added());
				// 处理删除变化 (FAQ: 与DataWatcher功能相同, 用于本地节点列表与ZK节点的不一致性恢复)
				this.deleted(event.getPath(), container.deleted());
			} catch (Throwable e) {
				throw new KeplerLocalException(e);
			}
		}

		/**
		 * 处理变化后新增节点
		 * 
		 * @param path
		 * @param children
		 */
		private void add(String path, List<String> children) {
			for (String child : children) {
				try {
					String actual = path + "/" + child;
					ServiceInstance instance = new DataWatcher(actual).snapshot();
					// 加载节点
					ZkContext.this.listener.add(instance);
					// 加载快照
					ZkContext.this.snapshot.instance(actual, instance);
					ZkContext.LOGGER.info("Reconfig and add instance: " + actual + " ( " + instance.host() + ") ");
				} catch (Throwable e) {
					ZkContext.LOGGER.error(e.getMessage(), e);
				}
			}
		}

		/**
		 * 处理变化后删除节点
		 * 
		 * @param path
		 * @param children
		 */
		private void deleted(String path, List<String> children) {
			for (String child : children) {
				try {
					String actual = path + "/" + child;
					// 获取并移除快照
					ServiceInstance instance = ZkContext.this.snapshot.instance(actual);
					// 多节点同时上线/下线时可能造成Instance已删除但依然调用Deleted方法
					if (instance != null) {
						ZkContext.this.listener.delete(instance);
						ZkContext.LOGGER.info("Reconfig and delete instance: " + actual + " ( " + instance.host() + ") ");
					}
				} catch (Throwable e) {
					ZkContext.LOGGER.error(e.getMessage(), e);
				}
			}
		}

		public List<String> snapshot() {
			return this.snapshot;
		}

		@Override
		public void process(WatchedEvent event) {
			ZkContext.LOGGER.info("Receive event: " + event);
			switch (event.getType()) {
			case NodeChildrenChanged:
				this.add(event);
				return;
			default:
				ZkContext.LOGGER.warn("Can not process event: " + event);
				return;
			}
		}
	}

	private class DataWatcher implements Watcher {

		private ServiceInstance data;

		private DataWatcher(String path) throws Exception {
			// 获取节点数据
			this.data = ZkContext.this.serials.def4input().input(ZkContext.this.zoo.getData(path, this, null), ServiceInstance.class);
		}

		public ServiceInstance snapshot() {
			return this.data;
		}

		@Override
		public void process(WatchedEvent event) {
			try {
				ZkContext.LOGGER.info("Receive event: " + event);
				switch (event.getType()) {
				case NodeDataChanged:
					ZkContext.this.listener.change(this.data, (this.data = ZkContext.this.serials.def4input().input(ZkContext.this.zoo.getData(event.getPath(), this, null), ServiceInstance.class)));
					return;
				case NodeDeleted:
					ZkContext.this.listener.delete(this.data);
					return;
				default:
					ZkContext.LOGGER.warn("Can not process event: " + event);
					return;
				}
			} catch (Throwable e) {
				throw new KeplerLocalException(e);
			}
		}
	}

	private class ConfigWatcher implements Watcher {

		private final String path;

		private ConfigWatcher(String path) throws Exception {
			// 监听Config节点变化
			ZkContext.this.zoo.exists((this.path = path), this);
		}

		public String path() {
			return this.path;
		}

		@SuppressWarnings("unchecked")
		private ConfigWatcher get(WatchedEvent event) {
			try {
				// Register Watcher for "getData" to avoid "set" failed
				ZkContext.this.config.config(ZkContext.this.serials.def4input().input(ZkContext.this.zoo.getData(event.getPath(), this, null), Map.class));
			} catch (Throwable throwable) {
				ZkContext.LOGGER.error(throwable.getMessage(), throwable);
			}
			return this;
		}

		private ConfigWatcher set() throws Exception {
			// 同步当前Config,保证ZK上节点数据为最新(移除(让Get中的GetDate Watcher失效), 重新发布)
			ZkContext.this.exports.destroy4config();
			ZkContext.this.config();
			return this;
		}

		@Override
		public void process(WatchedEvent event) {
			try {
				ZkContext.LOGGER.info("Receive event: " + event);
				switch (event.getType()) {
				case NodeDataChanged:
					this.get(event).set();
					return;
				case NodeDeleted:
					ZkContext.LOGGER.warn("Config: " + this.path + " will be deleted ... ");
					return;
				default:
					ZkContext.LOGGER.warn("ConfigWatcher can not process event: " + event);
					return;
				}
			} catch (Throwable e) {
				throw new KeplerLocalException(e);
			}
		}
	}

	private class DiffContainer<E extends Comparable<E>> {

		private final List<E> oldList;

		private final List<E> newList;

		private final List<E> elementAdded;

		private final List<E> elementDeleted;

		private DiffContainer(List<E> oldList, List<E> newList) {
			this.oldList = oldList;
			this.newList = newList;
			this.elementAdded = new ArrayList<E>(Math.max(oldList.size(), newList.size()));
			this.elementDeleted = new ArrayList<E>(Math.max(oldList.size(), newList.size()));
			this.calcDiff();
		}

		private void calcDiff() {
			int i = 0, j = 0;
			while (i < this.oldList.size() && j < this.newList.size()) {
				E eleA = this.oldList.get(i);
				E eleB = this.newList.get(j);
				if (eleA.compareTo(eleB) < 0) {
					this.elementDeleted.add(eleA);
					i++;
				} else if (eleA.compareTo(eleB) > 0) {
					this.elementAdded.add(eleB);
					j++;
				} else {
					i++;
					j++;
				}
			}
			for (; i < this.oldList.size(); i++) {
				this.elementDeleted.add(this.oldList.get(i));
			}
			for (; j < this.newList.size(); j++) {
				this.elementAdded.add(this.newList.get(j));
			}
		}

		public List<E> added() {
			return this.elementAdded;
		}

		public List<E> deleted() {
			return this.elementDeleted;
		}
	}

	/**
	 * 服务端口启动成功后, 再发布服务
	 * 
	 * @author tudesheng
	 */
	private class Delay {

		private List<Pair<Service, Object>> services = new ArrayList<Pair<Service, Object>>();

		private boolean started = false;

		public synchronized void exported(Service service, Object instance) throws Exception {
			if (this.started) {
				// 如果已启动则直接发布(场景: 断线重连)
				ZkContext.this.exported4delay(service, instance);
			} else {
				// 未重启则加入缓存
				this.services.add(new Pair<Service, Object>(service, instance));
			}
		}

		/**
		 * 触发延迟加载
		 * 
		 * @throws Exception
		 */
		public synchronized void reach() throws Exception {
			if (!this.started) {
				for (Pair<Service, Object> pair : services) {
					ZkContext.this.exported4delay(pair.key(), pair.val());
				}
				// 切换状态并清空缓存
				started = true;
				services = null;
			}
		}

	}

	private class Pair<K, V> {

		private K key;

		private V val;

		private Pair(K key, V val) {
			this.key = key;
			this.val = val;
		}

		public K key() {
			return key;
		}

		public V val() {
			return val;
		}
	}

	private class DelayInstall implements Delayed {

		/**
		 * 当前时间 + delay时间
		 */
		private final long deadline = TimeUnit.MILLISECONDS.convert(ZkContext.DELAY, TimeUnit.MILLISECONDS) + System.currentTimeMillis();

		private final Service service;

		private DelayInstall(Service service) {
			super();
			this.service = service;
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		public int compareTo(Delayed o) {
			return this.getDelay(TimeUnit.SECONDS) >= o.getDelay(TimeUnit.SECONDS) ? 1 : -1;
		}

		public Service service() {
			return this.service;
		}

		public String toString() {
			return "[deadline=" + this.deadline + "][service=" + this.service + "]";
		}
	}

	private class RefreshRunnable implements Runnable {

		private volatile boolean isRunning = false;

		@Override
		public void run() {
			if (this.isRunning) {
				return;
			}
			try {
				this.isRunning = true;
				Map<String, ServiceInstance> snapshot = new HashMap<String, ServiceInstance>(ZkContext.this.snapshot.instances);
				Map<String, ServiceInstance> current = new HashMap<String, ServiceInstance>();
				Set<Service> imported = ZkContext.this.snapshot.imported;
				for (Service service : imported) {
					String zkPath = ZkContext.this.road.road(ZkContext.ROOT, service.service(), service.versionAndCatalog());
					try {
						List<String> serviceNodeList = ZkContext.this.zoo.getChildren(zkPath, null);
						for (String serviceNode : serviceNodeList) {
							try {
								byte[] data = ZkContext.this.zoo.getData(zkPath + "/" + serviceNode, false, null);
								ServiceInstance serviceInstance = ZkContext.this.serials.def4input().input(data, ServiceInstance.class);
								current.put(zkPath + "/" + serviceNode, serviceInstance);
							} catch (NodeExistsException e) {
								ZkContext.LOGGER.warn("Concurrent case. Node not exists");
							}

						}
					} catch (Exception e) {
						ZkContext.LOGGER.error("Zk operation error. " + e.getMessage(), e);
					}
				}
				this.handle(current, snapshot);
				ZkContext.this.snapshot.instances = current;
			} catch (Exception e) {
				ZkContext.LOGGER.error(e.getMessage(), e);
			} finally {
				this.isRunning = false;
			}
		}

		private void handle(Map<String, ServiceInstance> current, Map<String, ServiceInstance> snapshot) throws Exception {
			List<ServiceInstance> added = new ArrayList<ServiceInstance>();
			List<ServiceInstance> removed = new ArrayList<ServiceInstance>();
			List<ServiceInstance[]> modified = new ArrayList<ServiceInstance[]>();
			ZkContext.LOGGER.info("Begin handling change.");
			ZkContext.LOGGER.info("Now: " + current.keySet() + " Before: " + snapshot.keySet());
			for (String currentNode : current.keySet()) {
				if (!snapshot.containsKey(currentNode)) {
					// new node
					ZkContext.LOGGER.info("Add node " + currentNode + ", " + current.get(currentNode));
					added.add(current.get(currentNode));
				} else {
					if (current.get(currentNode).host().propertyChanged(snapshot.get(currentNode).host())) {
						ZkContext.LOGGER.info("Modify node " + currentNode + ", " + current.get(currentNode));
						modified.add(new ServiceInstance[] { snapshot.get(currentNode), current.get(currentNode) });
					}
				}
			}
			for (String snapshotNode : snapshot.keySet()) {
				if (!current.containsKey(snapshotNode)) {
					ZkContext.LOGGER.info("Remove node " + snapshotNode + ", " + snapshot.get(snapshotNode));
					removed.add(snapshot.get(snapshotNode));
				}
			}
			this.handleAdded(added);
			this.handleRemoved(removed);
			this.handleModified(modified);
		}

		private void handleAdded(List<ServiceInstance> added) throws Exception {
			for (ServiceInstance serviceInstance : added) {
				ZkContext.LOGGER.info("[new node found]" + serviceInstance);
				ZkContext.this.listener.add(serviceInstance);
			}
		}

		private void handleRemoved(List<ServiceInstance> removed) throws Exception {
			for (ServiceInstance serviceInstance : removed) {
				ZkContext.LOGGER.info("[node removed]" + serviceInstance);
				ZkContext.this.listener.delete(serviceInstance);
			}
		}

		private void handleModified(List<ServiceInstance[]> modified) throws Exception {
			for (ServiceInstance[] serviceInstance : modified) {
				ZkContext.LOGGER.info("[node update]" + serviceInstance[0]);
				ServiceInstance oldSrvInst = serviceInstance[0], newSrvInst = serviceInstance[1];
				ZkContext.this.listener.change(oldSrvInst, newSrvInst);
			}
		}
	}
}
