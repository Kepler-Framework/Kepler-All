package com.kepler.queue;

import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface QueueExecutor {
	
	public boolean executor(Request request, QueueRunnable runnable);
}
