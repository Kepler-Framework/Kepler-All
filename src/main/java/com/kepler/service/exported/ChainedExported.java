package com.kepler.service.exported;

import java.util.List;

import com.kepler.service.Exported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月8日
 */
public class ChainedExported implements Exported {

	private final List<Exported> exported;

	public ChainedExported(List<Exported> exported) {
		super();
		this.exported = exported;
	}

	@Override
	public void exported(Service service, Object instance) throws Exception {
		for (Exported each : this.exported) {
			each.exported(service, instance);
		}
	}
}
