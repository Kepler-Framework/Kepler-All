package com.kepler.host;

import java.util.List;

/**
 * @author kim 2015年7月9日
 */
public interface Hosts {

	public void wait(Host host);

	public void active(Host host);

	public void remove(Host host);

	public void replace(Host current, Host newone);

	public boolean ban(Host host);

	/**
	 * 是否包含指定主机
	 * 
	 * @param host
	 * @return
	 */
	public boolean contain(Host host);

	/**
	 * 获取默认主机集合
	 * @return
	 */
	public List<Host> main();

	/**
	 * 获取指定标签主机集合
	 * @param tag
	 * @return
	 */
	public List<Host> tags(String tag);
	
	/**
	 * 获取指定状态的主机
	 * 
	 * @param state
	 * @return
	 */
	public List<Host> select(HostState state);

	/**
	 * 获取指定地址的主机
	 * 
	 * @param address
	 * @return
	 */
	public Host select(String sid);
}
