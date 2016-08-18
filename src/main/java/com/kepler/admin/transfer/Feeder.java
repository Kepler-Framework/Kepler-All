package com.kepler.admin.transfer;

import java.util.Collection;

import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;

/**
 * @author kim 2015年7月22日
 */
@Service(version = "0.0.1")
@Internal
public interface Feeder {

	public void feed(long timestamp, Collection<Transfers> transfer);
}
