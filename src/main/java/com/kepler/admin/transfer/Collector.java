package com.kepler.admin.transfer;

import java.util.Collection;

import com.kepler.ack.Ack;

/**
 * @author kim 2015年7月21日
 */
public interface Collector {
	
	public Transfer peek(Ack ack);

	public Transfer collect(Ack ack);
	
	public Collection<Transfers> transfers();
}
