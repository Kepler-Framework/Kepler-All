package com.kepler.admin.transfer;

import java.io.Serializable;
import java.util.Collection;

import com.kepler.ack.Status;
import com.kepler.host.Host;

/**
 * @author kim 2015年7月24日
 */
public interface Transfers extends Serializable {

	public String service();

	public String version();

	public String method();

	/**
	 * 清理
	 */
	public void clear();

	/**
	 * 重置
	 */
	public void reset();

	public Collection<Transfer> transfers();

	public Transfer get(Host local, Host remote);

	public Transfer put(Host local, Host remote, Status status, long rtt);
}
