package com.kepler.admin.trace;

import java.util.List;

import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;
import com.kepler.trace.TraceCause;

/**
 * @author KimShen
 *
 */
@Service(version = "0.0.1")
@Internal
public interface Feeder {

	public void feed(List<TraceCause> cause);
}
