package com.kepler.service.imported;

import java.util.List;

import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月24日
 */
public class ChainedImported implements Imported {

	private final List<Imported> imported;

	public ChainedImported(List<Imported> imported) {
		super();
		this.imported = imported;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		for (Imported each : this.imported) {
			each.subscribe(service);
		}
	}
}
