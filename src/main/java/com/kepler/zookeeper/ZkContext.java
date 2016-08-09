package com.kepler.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import com.kepler.KeplerLocalException;
import com.kepler.admin.status.Status;
import com.kepler.config.Config;
import com.kepler.config.ConfigSync;
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
public class ZkContext implements Demotion, Imported, Exported, ConfigSync, ApplicationListener<ContextRefreshedEvent> {

	public static final String DEPENDENCY = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".dependency", "_dependency");

	public static final String CONFIG = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".config", "_configs");

	public static final String STATUS = PropertiesUtils.get(ZkContext.class.getName().toLowerCase() + ".status", "_status");

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

	private static final Log LOGGER = LogFactory.getLog(ZkContext.class);

	private final ZkWatcher watcher = new ZkWatcher();

	private final Snapshot snapshot = new Snapshot();

	private final Exports exports = new Exports();

	private final Roadmap road = new Roadmap();

	private final ImportedListener listener;

	private final ServerHost local;

	private final Profile profile;

	private final Serials serials;

	private final Status status;

	private final Config config;

	private final ZkClient zoo;

	public ZkContext(ImportedListener listener, ServerHost local, Serials serials, Profile profile, Config config, Status status, ZkClient zoo) {
		super();
		this.zoo = zoo.bind(this);
		this.listener = listener;
		this.profile = profile;
		this.serials = serials;
		this.config = config;
		this.status = status;
		this.local = local;
	}

	/**
	 * 重新导入服务
	 * 
	 * @throws Exception
	 */
	private void reset4imported() throws Exception {
		for (Service service : this.snapshot.imported) {
			this.subscribe(service);
		}
	}

	/**
	 * 重新发布服务
	 * 
	 * @throws Exception
	 */
	private void reset4exported() throws Exception {
		for (Service service : this.snapshot.exported.keySet()) {
			this.exported(service, this.snapshot.exported.get(service));
		}
	}

	/**
	 * 发布Status节点
	 * 
	 * @throws Exception
	 */
	private void status() throws Exception {
		if (this.exports.status()) {
			this.exports.status(this.zoo.create(this.road.mkdir(new StringBuffer(ZkContext.ROOT).append(ZkContext.STATUS).toString()) + "/" + this.local.sid(), this.serials.def4output().output(new DefaultHostStatus(this.local, this.status.get()), HostStatus.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
		}
	}

	/**
	 * 发布Config节点(并监听)
	 * 
	 * @throws Exception
	 */
	private void config() throws Exception {
		if (this.exports.config()) {
			this.exports.config(new ConfigWatcher(this.zoo.create(this.road.mkdir(new StringBuffer(ZkContext.ROOT).append(ZkContext.CONFIG).toString()) + "/" + this.local.sid(), this.serials.def4output().output(PropertiesUtils.properties(), Map.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)).path());
		}
	}

	/**
	 * 将本地的Config同步到ZooKeeper
	 */
	@Override
	public void sync() throws Exception {
		// 注销后重新发布
		this.exports.destroy4config();
		this.config();
	}

	/**
	 * 发布服务依赖
	 * 
	 * @param service
	 * @throws Exception
	 */
	private void dependency(Service service) throws Exception {
		if (PropertiesUtils.profile(this.profile.profile(service), ZkContext.DEPENDENCY_KEY, ZkContext.DEPENDENCY_VAL)) {
			this.zoo.create(this.road.mkdir(this.road.road(new StringBuffer(ZkContext.ROOT).append(ZkContext.DEPENDENCY).toString(), service.service(), service.versionAndCatalog())) + "/", this.serials.def4output().output(new ImportedService(this.local, service), ImportedService.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		}
	}

	public void demote() throws Exception {
		this.exports.demote();
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		this.exports.destroy();
		this.zoo.close();
	}

	/**
	 * 重置/重连
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception {
		// 注销已发布服务
		this.exports.destroy();
		// 重新发布
		this.reset4exported();
		this.reset4imported();
		this.status();
		this.config();
	}

	@Override
	public void subscribe(Service service) throws Exception {
		if (!PropertiesUtils.profile(this.profile.profile(service), ZkContext.IMPORT_KEY, ZkContext.IMPORT_VAL)) {
			ZkContext.LOGGER.warn("Disabled import service: " + service + " ... ");
			return;
		}
		// 订阅服务并启动Watcher监听
		this.watcher.watch(this.road.road(ZkContext.ROOT, service.service(), service.versionAndCatalog()));
		// 加入本地服务依赖列表
		this.snapshot.subscribe(service);
		this.dependency(service);
		ZkContext.LOGGER.info("Import service: " + service);
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		if (!PropertiesUtils.profile(this.profile.profile(service), ZkContext.EXPORT_KEY, ZkContext.EXPORT_VAL)) {
			ZkContext.LOGGER.warn("Disabled export service: " + service + " ... ");
			return;
		}
		// 生成ZK节点(Profile Tag, Priority)
		ZkSerial serial = new ZkSerial(new Builder(this.local).setTag(PropertiesUtils.profile(this.profile.profile(service), Host.TAG_KEY, Host.TAG_VAL)).setPriority(Integer.valueOf(PropertiesUtils.profile(this.profile.profile(service), Host.PRIORITY_KEY, Host.PRIORITY_DEF))).toServerHost(), service);
		// 加入已导出服务列表(Path -> Instance)
		this.exports.put(this.zoo.create(this.road.mkdir(this.road.road(ZkContext.ROOT, service.service(), service.versionAndCatalog())) + "/", this.serials.def4output().output(serial, ServiceInstance.class), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL), serial);
		// 加入已导出服务列表(Service -> Instance)
		this.snapshot.exported(service, instance);
		ZkContext.LOGGER.info("Export service: " + service + " ... ");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			// 启动完毕后发布Status/Config节点
			this.status();
			this.config();
		} catch (Throwable throwable) {
			throw new KeplerLocalException(throwable);
		}
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

	private class Exports {

		/**
		 * 已发布服务(Path -> Instance)，使用ConcurrentHashMap防止反初始化时有主机同时注册导致注销失败
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

		public void status(String status) {
			this.status = status;
		}

		public void config(String config) {
			this.config = config;
		}

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
				ZkContext.LOGGER.warn(e.getMessage(), e);
			}
		}

		/**
		 * 注销Status节点
		 */
		public void destroy4status() {
			if (status != null) {
				this.destroy(this.status);
				this.status = null;
			}
		}

		/**
		 * 注销Config节点
		 */
		public void destroy4config() {
			if (config != null) {
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
		 * 已发布服务及其底层Bean
		 */
		private final Map<Service, Object> exported = new HashMap<Service, Object>();

		/**
		 * 已导入服务
		 */
		private final Set<Service> imported = new HashSet<Service>();

		@Override
		public void subscribe(Service service) throws Exception {
			this.imported.add(service);
		}

		@Override
		public void exported(Service service, Object instance) throws Exception {
			this.exported.put(service, instance);
		}
	}

	private class ZkWatcher {

		public void watch(String path) throws Exception {
			try {
				// 获取所有Children Path,并监听路径变化
				for (String child : new PathWatcher(path).snapshot()) {
					this.watch(path, child);
				}
			} catch (NoNodeException e) {
				// 节点不存在提示INFO
				ZkContext.LOGGER.info(e.getMessage(), e);
			} catch (Throwable e) {
				ZkContext.LOGGER.error(e.getMessage(), e);
			}
		}

		/**
		 * 初始化(Add)已注册服务,并监听节点变化
		 * 
		 * @param path
		 * @param child
		 */
		private void watch(String path, String child) {
			try {
				ZkContext.this.listener.add(new DataWatcher(path + "/" + child).snapshot());
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
				for (String child : new DiffContainer<String>(previous, this.snapshot).added()) {
					this.add(event.getPath(), child);
				}
			} catch (Throwable e) {
				throw new KeplerLocalException(e);
			}
		}

		private void add(String path, String child) {
			try {
				// DataWatcher数据变化监听
				ZkContext.this.listener.add(new DataWatcher(path + "/" + child).snapshot());
			} catch (Throwable e) {
				ZkContext.LOGGER.info(e.getMessage(), e);
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
			// 再次同步当前主机Config,保证ZK上节点数据为最新
			ZkContext.this.sync();
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

		private final List<E> elementRemoved;

		private DiffContainer(List<E> oldList, List<E> newList) {
			this.oldList = oldList;
			this.newList = newList;
			this.elementAdded = new ArrayList<E>(Math.max(oldList.size(), newList.size()));
			this.elementRemoved = new ArrayList<E>(Math.max(oldList.size(), newList.size()));
			this.calcDiff();
		}

		private void calcDiff() {
			int i = 0, j = 0;
			while (i < this.oldList.size() && j < this.newList.size()) {
				E eleA = this.oldList.get(i);
				E eleB = this.newList.get(j);
				if (eleA.compareTo(eleB) < 0) {
					this.elementRemoved.add(eleA);
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
				this.elementRemoved.add(this.oldList.get(i));
			}
			for (; j < this.newList.size(); j++) {
				this.elementAdded.add(this.newList.get(j));
			}
		}

		public List<E> added() {
			return this.elementAdded;
		}
	}
}
