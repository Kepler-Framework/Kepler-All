package com.kepler.zookeeper;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * @author kim 2015年11月19日
 */
public class ZkClient {

	private static final Log LOGGER = LogFactory.getLog(ZkClient.class);
	
	private ZkContext context;

	private ZooKeeper zoo;

	/**
	 * 底层ZooKeeper
	 * 
	 * @return
	 */
	public ZooKeeper zoo() {
		return this.zoo;
	}

	public ZkClient bind(ZooKeeper zoo) {
		this.zoo = zoo;
		ZkClient.LOGGER.info("Binding ZooKeeper: " + zoo);
		return this;
	}

	public ZkClient bind(ZkContext context) {
		this.context = context;
		ZkClient.LOGGER.info("Binding ZkContext: " + context);
		return this;
	}

	public String create(String path, byte data[], List<ACL> acl, CreateMode createMode) throws Exception {
		return this.zoo.create(path, data, acl, createMode);
	}

	public byte[] getData(String path, Watcher watcher, Stat stat) throws Exception {
		return this.zoo.getData(path, watcher, stat);
	}

	public byte[] getData(String path, boolean watcher, Stat stat) throws Exception {
		return this.zoo.getData(path, watcher, stat);
	}

	public List<String> getChildren(String path, Watcher watcher) throws Exception {
		return this.zoo.getChildren(path, watcher);
	}

	public List<String> getChildren(String path, boolean watcher) throws Exception {
		return this.zoo.getChildren(path, watcher);
	}

	public void setData(String path, byte[] data, int version) throws Exception {
		this.zoo.setData(path, data, version);
	}

	public Stat exists(String path, Watcher watch) throws Exception {
		return this.zoo.exists(path, watch);
	}

	public Stat exists(String path, boolean watch) throws Exception {
		return this.zoo.exists(path, watch);
	}

	public void delete(String path, int version) throws Exception {
		this.zoo.delete(path, version);
	}

	public void close() throws Exception {
		this.zoo.close();
	}

	/**
	 * 重置
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception {
		this.context.reset();
	}
}
