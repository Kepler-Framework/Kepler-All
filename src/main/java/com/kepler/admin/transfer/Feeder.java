package com.kepler.admin.transfer;

import java.util.Collection;

import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;

/**
 * @author kim 2015年7月22日
 */
@Service(version = "0.0.7")
@Internal
public interface Feeder {

	public void feed(Collection<Transfers> transfer);
}
