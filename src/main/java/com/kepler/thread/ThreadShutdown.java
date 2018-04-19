package com.kepler.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author KimShen
 *
 */
abstract public class ThreadShutdown {

	private static final Log LOGGER = LogFactory.getLog(ThreadShutdown.class);

	private void waitingRunnable(ExecutorService executor, long timestamp) throws Exception {
		while (!executor.awaitTermination(this.interval(), TimeUnit.MILLISECONDS)) {
			ThreadShutdown.LOGGER.info("Shutdown threads using " + (TimeUnit.SECONDS.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)) + "s ...");
		}
	}

	private void shutdown4immediately(ExecutorService executor) throws Exception {
		for (Runnable each : executor.shutdownNow()) {
			ThreadShutdown.LOGGER.warn("Shutdown threads, lossing " + each.getClass() + " ... ");
		}
		this.waitingRunnable(executor, System.currentTimeMillis());
	}

	private void shutdown4waiting(ExecutorService executor) throws Exception {
		executor.shutdown();
		this.waitingRunnable(executor, System.currentTimeMillis());
	}

	protected void destroy(ExecutorService executor) throws Exception {
		if (this.waiting()) {
			this.shutdown4waiting(executor);
		} else {
			this.shutdown4immediately(executor);
		}
	}

	abstract protected boolean waiting();

	abstract protected int interval();

}
