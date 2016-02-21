package com.kepler.host;

import java.util.List;

/**
 * @author kim 2015年7月15日
 */
public interface HostSelector {

	/**
	 * 所有主机
	 * 
	 * @return
	 */
	public List<Host> all();

	/**
	 * Host using def tag
	 * 
	 * @return
	 */
	public List<Host> main();

	/**
	 * Host with tag
	 * 
	 * @param tag
	 * @return
	 */
	public List<Host> tags(String tag);
}
