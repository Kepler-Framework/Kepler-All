package com.kepler.admin.transfer;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author kim 2015年7月24日
 */
public interface Transfers extends Serializable {

	public String service();

	public String version();

	public String method();

	/**
	 * 多个Local -> Target
	 * 
	 * @return
	 */
	public Collection<Transfer> transfers();
}
