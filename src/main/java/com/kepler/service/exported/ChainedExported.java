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
	public void export(Service service, Object instance) throws Exception {
		synchronized (this.exported) {
			for (Exported each : this.exported) {
				each.export(service, instance);
			}
		}
	}

	public void logout(Service service) throws Exception {
		synchronized (this.exported) {
			for (Exported each : this.exported) {
				each.logout(service);
			}
		}
	}
}
