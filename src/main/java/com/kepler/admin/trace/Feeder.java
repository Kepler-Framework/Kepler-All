package com.kepler.admin.trace;

import java.util.List;

import com.kepler.annotation.Async;
import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;
import com.kepler.host.Host;
import com.kepler.trace.TraceCause;

/**
 * @author KimShen
 *
 */
@Service(version = "0.0.8")
@Internal
public interface Feeder {

	/**
	 * @param host 本地主机
	 * @param cause 
	 */
	@Async
	public void feed(Host host, List<TraceCause> cause);
}
