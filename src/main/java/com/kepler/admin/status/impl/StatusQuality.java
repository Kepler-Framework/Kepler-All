package com.kepler.admin.status.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.status.Status;
import com.kepler.quality.Quality;

/**
 * @author KimShen
 *
 */
public class StatusQuality implements Status {

	private final Map<String, Object> status = new HashMap<String, Object>();

	private final Quality quality;

	public StatusQuality(String[] fields, Quality quality) {
		this.quality = quality;
	}

	public StatusQuality(Quality quality) {
		this.quality = quality;
	}

	@Override
	public Map<String, Object> get() {
		this.status.put("quality_breaking", this.quality.getBreakingAndReset());
		this.status.put("quality_demoting", this.quality.getDemotingAndReset());
		this.status.put("quality_waiting", this.quality.getWaitingAndReset());
		this.status.put("quality_idle", this.quality.getIdleAndReset());
		return this.status;
	}
}
