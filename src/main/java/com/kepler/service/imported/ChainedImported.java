package com.kepler.service.imported;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.service.Imported;
import com.kepler.service.Service;

/**
 * @author kim 2015年7月24日
 */
public class ChainedImported implements Imported {

	private static final Log LOGGER = LogFactory.getLog(ChainedImported.class);

	private final List<Imported> imported;

	public ChainedImported(List<Imported> imported) {
		super();
		this.imported = imported;
	}

	@Override
	public void subscribe(Service service) throws Exception {
		synchronized (this.imported) {
			ChainedImported.LOGGER.info("[subscribe-chained][service=" + service + "]");
			for (Imported each : this.imported) {
				each.subscribe(service);
			}
		}
	}

	public void unsubscribe(Service service) throws Exception {
		synchronized (this.imported) {
			ChainedImported.LOGGER.info("[unsubscribe-chained][service=" + service + "]");
			for (Imported each : this.imported) {
				each.unsubscribe(service);
			}
		}
	}
}
